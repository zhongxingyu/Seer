 package ee.ant.pimap.download;
 
 import ee.ant.pimap.base.*;
 import ee.ant.pimap.db.PiConnection;
 import org.apache.commons.codec.binary.Base64;
 import org.openqa.selenium.*;
 import org.openqa.selenium.Point;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.Select;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.sql.*;
 import java.util.*;
 import java.util.List;
 
 import static org.openqa.selenium.OutputType.BASE64;
 
 /**
  *
  */
 public class MapFetcher {
 
 	private static Connection conn = null;
 	private static ChromeDriver driver = null;
 	private static ScanDirection direction = ScanDirection.RIGHT;
 
 	public static void main (String[] args) {
 		conn = PiConnection.connect("jdbc:derby:c:/Users/User/IdeaProjects/personal/pimap/db/pi");
 		driver = new ChromeDriver();
 		if (conn != null) {
 			try {
 				login();
 				// it is important that first cell is visible on the map
				scanMapFrom(new Tile(86, 474));
 			} catch (IOException e) {
 				System.out.println(e.getMessage());
 			} catch (InterruptedException e) {
 				System.out.println(e.getMessage());
 			}
 			PiConnection.shutdown();
 			System.out.println("Disconnected from DB");
 		}
 	}
 
 	/**
 	 * Scans entire map from start point to the right and then down. Navigation is executed by "clicking" border elements
 	 * @param startPoint cell to scan from. Must be visible
 	 */
 	private static void scanMapFrom(Tile startPoint) throws IOException, InterruptedException {
 		driver.navigate().to("http://w14.wofh.ru/mapinfo?o=0&x="+ startPoint.getX() + "&y=" + startPoint.getY());
 		WebElement centerEl = (new WebDriverWait(driver,10)).until(ExpectedConditions.presenceOfElementLocated(By.className("a_cntr")));
 		centerEl.click(); // center map on the start point and switch to map view
 		(new WebDriverWait(driver,10)).until(ExpectedConditions.presenceOfElementLocated(By.id("mapspace22"))); //wait until map page loads
 		Thread.sleep(2000);
 		boolean isEnd = false;
 		Tile centerPoint = startPoint;
 		// now we are ready to navigate on the map.
 		while (!isEnd) {
 			// calculate chunks to be present on the visible region
 			Region region = new Region (centerPoint, driver);
 			List<Tile> tiles = region.getTiles();
 			// create list of cells to save to DB
 			for (Iterator<Tile> tileItr = tiles.iterator(); tileItr.hasNext(); ) {
 				Tile tile = tileItr.next();
 				if (tile.getClimate() != ClimateType.UNKNOWN  && !cellInDb(tile)) {
 					String divClass = "tile x"+tile.getX()+" y"+tile.getY();
 					tile.setPicture(getImage("//div[@class='" + divClass + "']"));
 				} else {
 					tileItr.remove();
 				}
 			}
 			saveTiles(tiles);
 			centerPoint = moveNext(centerPoint);
 			if (centerPoint == null) {
 				isEnd = true;
 				System.out.println("Scan done!");
 			} else {
 				System.out.println("Moved to point:"+ centerPoint.getX() +"," + centerPoint.getY());
 			}
 		}
 	}
 
 	private static Tile moveNext(Tile centerPoint) {
 		// one navigation step is 7 cells
 		int nextX =  (direction == ScanDirection.RIGHT) ? centerPoint.getX() + 7: centerPoint.getX() - 7;
 		if (nextX > 479) {
 			int nextY = centerPoint.getY() + 7;
 			if (nextY > 479) return null; // this is the end of the cycle scanning entire map
 			Tile nextTile = new Tile(centerPoint.getX(), nextY);
 			WebElement downBtn = driver.findElement(By.className("bord_b"));
 			downBtn.click();
 			direction = ScanDirection.LEFT;
 			return nextTile;
 		} else if (nextX < 0) {
 			int nextY = centerPoint.getY() + 7;
 			if (nextY > 479) return null; // this is the end of the cycle scanning entire map
 			Tile nextTile = new Tile(centerPoint.getX(), nextY);
 			WebElement downBtn = driver.findElement(By.className("bord_b"));
 			downBtn.click();
 			direction = ScanDirection.RIGHT;
 			return nextTile;
 		} else {
 			Tile nextTile = new Tile(nextX, centerPoint.getY());
 			WebElement btn;
 			if (direction == ScanDirection.RIGHT) {
 				btn = driver.findElement(By.className("bord_r"));
 			}  else {
 				btn = driver.findElement(By.className("bord_l"));
 			}
 			btn.click();
 			return nextTile;
 		}
 	}
 
 	/**
 	 * Executes login procedure
 	 */
 	private static void login() {
 		Properties props = new Properties();
 		try {
 			props.load(MapFetcher.class.getClassLoader().getResourceAsStream("connection.properties"));
 			driver.get(props.getProperty("loginURL"));
 			WebElement username = driver.findElement(By.id("fldNameOrEmail"));
 			username.sendKeys(props.getProperty("login"));
 			WebElement password = driver.findElement(By.id("fldPassword"));
 			password.sendKeys(props.getProperty("password"));
 			Select world = new Select(driver.findElement(By.id("fldCheckWorld")));
 			world.selectByValue(props.getProperty("world"));
 			WebElement loginBtn = driver.findElement(By.className("btn_mlog"));
 			loginBtn.click();
 			(new WebDriverWait(driver,10)).until(ExpectedConditions.presenceOfElementLocated(By.id("m2_map"))); //wait until page loads
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	/**
 	 * Save cells to database
 	 * @param tiles list of cells
 	 */
 	private static void saveTiles(List<Tile> tiles) {
 		String sql = "INSERT INTO MAPCELL (COORD_X, COORD_Y, TERRAIN, CLIMATE, POLLUTION, HAS_FOG, STYLE, TILE, DEPOSIT_ID, TOWN_ID) " +
 				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 		try {
 			PreparedStatement pstmt = conn.prepareStatement(sql);
 			for (Tile tile : tiles) {
 				int townId = 0; int depositId = 0;
 				if (tile.getTown() != null) {
 					townId = saveTown(tile.getTown());
 				}
 				if (tile.getDeposit() != DepositType.NONE) {
 					depositId = saveDeposit(tile.getDeposit(), townId);
 				}
 				pstmt.setInt(1, tile.getX());
 				pstmt.setInt(2, tile.getY());
 				pstmt.setString(3, tile.getTerrain().name());
 				pstmt.setString(4, tile.getClimate().name());
 				pstmt.setInt(5, tile.getPollution());
 				pstmt.setBoolean(6, tile.hasFog());
 				pstmt.setString(7, tile.getStyle());
 				pstmt.setBlob(8, new ByteArrayInputStream(tile.getPicture()));
 				pstmt.setInt(9, depositId);
 				pstmt.setInt(10, townId);
 				pstmt.executeUpdate();
 			}
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static int saveDeposit(DepositType type, int townId) {
 		String sql = "INSERT INTO DEPOSIT (TYPE, TOWN_ID) VALUES (?, ?)";
 		try {
 			PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
 			pstmt.setString(1, type.name());
 			pstmt.setInt(2, townId);
 			pstmt.executeUpdate();
 			ResultSet rs = pstmt.getGeneratedKeys();
 			if (rs.next()) {
 				return rs.getInt(1);
 			} else {
 				return -1;
 			}
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			return -1;
 		}
 	}
 
 	private static int saveTown(Town town) {
 		// first check if town with combination player+name exists
 		String sql = "SELECT ID FROM TOWN WHERE PLAYER=? AND NAME=?";
 		try {
 			PreparedStatement pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, town.getPlayer());
 			pstmt.setString(2, town.getName());
 			ResultSet rs = pstmt.executeQuery();
 			if (rs.next()) {
 				return rs.getInt(1); // return town_id if found
 			}
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			return -1;
 		}
 		// insert new record if there is none
 		sql = "INSERT INTO TOWN (NAME, PLAYER, COUNTRY, POPULATION, RACE, WONDER) VALUES (?, ?, ?, ?, ?, ?)";
 		try {
 			PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
 			pstmt.setString(1, town.getName());
 			pstmt.setString(2, town.getPlayer());
 			pstmt.setString(3, town.getCountry());
 			pstmt.setInt(4, town.getPopulation());
 			pstmt.setString(5, town.getRace().name());
 			pstmt.setString(6, town.getWonder().name());
 			pstmt.executeUpdate();
 			ResultSet rs = pstmt.getGeneratedKeys();
 			if (rs.next()) {
 				return rs.getInt(1);
 			} else {
 				return -1;
 			}
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			return -1;
 		}
 	}
 
 	/**
 	 * Check if cell with coordinates (x,y) already saved in DB
 	 * @param tile query cell
 	 * @return true if cell exists in database
 	 */
 	private static boolean cellInDb(Tile tile) {
 		boolean result = false;
 		String sql = "SELECT COUNT(*) FROM MAPCELL WHERE COORD_X=? AND COORD_Y=?";
 		try {
 			PreparedStatement pstmt = conn.prepareStatement(sql);
 			pstmt.setInt(1, tile.getX());
 			pstmt.setInt(2, tile.getY());
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				int count = rs.getInt(1);
 				if (count > 0) return true;
 			}
 			rs.close();
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 		}
 		return result;
 	}
 
 	/**
 	 * Gets element image
 	 * @param elementXpath map tile xpath
 	 * @return PNG image as byte[]
 	 */
 	private static byte[] getImage(String elementXpath) {
 		String screenB64 = driver.getScreenshotAs(BASE64);
 		WebElement element = driver.findElement(By.xpath(elementXpath));
 		ByteArrayInputStream screenIn = new ByteArrayInputStream(Base64.decodeBase64(screenB64));
 		ByteArrayOutputStream screenOut = new ByteArrayOutputStream();
 		Point p = element.getLocation();
 
 		//Detect element size and save it as PNG image
 		int width = element.getSize().getWidth();
 		int height = element.getSize().getHeight();
 		Rectangle rect = new Rectangle(width, height);
 		try {
 			BufferedImage img = ImageIO.read(screenIn);
 			BufferedImage dest = img.getSubimage(p.getX(), p.getY(), rect.width, rect.height);
 			ImageIO.write(dest, "png", screenOut);
 			return screenOut.toByteArray();
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 		}
 		return null;
 	}
 
 }

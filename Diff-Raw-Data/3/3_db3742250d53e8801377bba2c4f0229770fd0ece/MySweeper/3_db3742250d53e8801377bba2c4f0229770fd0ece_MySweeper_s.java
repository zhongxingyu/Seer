 package kr.co.ddononi;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 import javax.swing.text.AbstractDocument.Content;
 
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.Segment;
 import net.htmlparser.jericho.Source;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.omg.CORBA.Request;
 
 
 public class MySweeper {
 	private static void showMenu(){
 		System.out.println("-------------------------------");
 		System.out.println("              TuTuSweeper      ");
 		System.out.println("-------------------------------");
 		System.out.println("     Խּҷ ϱ 1                       ");
 		System.out.println("     ̵ ϱ 2                       ");
 		System.out.println("     丮 ϱ       3                       ");
 		System.out.println("-------------------------------");
 		System.out.println("   made by ddononi (ver 1.10)  ");
 		System.out.println("-------------------------------");
 		System.out.print  ("     ޴ Էϼ :              ");
 //		String s = "<script>document.domain='fewoo.net';</script>		<script language=\"JavaScript\">\n"
 //		+ "window.opener.insertImageSrc(\"http://club.fewoo.net/dramaworld/data/__132808007733647.jpg\");\n;"
 //		+ "window.close();</script>";
 	}
 
 	/**
 	 * ̵ ̿Ͽ Content Ѵ.
 	 */
 	private void searchFromId(){
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				System.in)); // ŰԷ½Ʈ
 		int count = 0;
 		String line;
 		try {
 			while(true){
 				System.out.print("̵ Էϼ : ");
 				line = "";
 					line = reader.readLine().trim();
 				if(line.equalsIgnoreCase("bye")){ //	
 					break;
 				}else if(line.length() < 5){
 					continue;
 				}
 			//  ð üũ
 				long startTime = System.currentTimeMillis();
 
 				TuTuSweeper sp = new TuTuSweeper();
 				sp.getStartContent(line);
 				long endTime = System.currentTimeMillis();
 				endTime = (endTime - startTime);
 				System.out.println("ڷ  ð : " + endTime + "Millis" );
 				count++;
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			try {
 				reader.close();
 			} catch (IOException e) {}
 		}
 
 		System.out.println("  ڷ : " + count + "");
 		System.out.println("bye~ bye~");
 	}
 
 	private void searchFromUrl(){
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				System.in)); // ŰԷ½Ʈ
 		int count = 0;
 		String url;
 		try {
 			while(true){
 				System.out.print("ּҸ Էϼ : ");
 				url = "";
 				url = reader.readLine();
 				if(url.equalsIgnoreCase("bye")){ //	
 					break;
 				}else if(url.length() < 10){
 					continue;
 				}
 				//  ð üũ
 				long startTime = System.currentTimeMillis();
 
 				TuTuSweeper sp = new TuTuSweeper();
 				sp.getStartMultiContents(url);
 
 				long endTime = System.currentTimeMillis();
 				endTime = (endTime - startTime) / 1000;
 				System.out.println("Խ ڷ  ð : " + endTime + "" );
 				count++;
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			try {
 				reader.close();
 			} catch (IOException e) {}
 		}
 
 		System.out.println("  ڷ : " + count + "");
 		System.out.println("bye~ bye~");
 	}
 
 	//private String dir;
 	public static void main(final String[] args){
 		/*
 		if(args.length < 1) {
 			System.err.println("Usage: java TutuSweeper [tutu-id Numbers]... ");
 			System.exit(1);
 		}
 		*/
 
 		showMenu();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				System.in)); // ŰԷ½Ʈ
 		String selNum;
 		try {
 			while(true){
 				reader.ready();
 				selNum = reader.readLine();
 				if(selNum.equals("1")){
 					System.out.println("\n Խ ּ ˻մϴ!\n");
 					MySweeper tutu = new MySweeper();
 					tutu.searchFromUrl();
 					break;
 				}else if(selNum.equals("2")){
 					System.out.println("\n̵ ˻մϴ!\n");
 					MySweeper tutu = new MySweeper();
 					tutu.searchFromId();
 					break;
 				}else{
 					System.out.println(selNum);
 					System.out.println("\nȮ ޴ ϼ!!\n");
 					showMenu();
 				}
 			}
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			System.out.println("Ȯ ޴ ϼ");
 		}finally{
 			try {
 				reader.close();
 			} catch (IOException e) {}
 		}
 
 	}
 }
 
 
 class TuTuSweeper{
 	private String dir = null;
 	private String id = null;
 	public final static String TUTU_URL =
 			"http://www.tutudisk.com//main/popup/bbs_info_0.php?idx=";	// ϵ ּ
 	public final static String SAVE_FILE_NAME = "content.txt";			// ϸ
 	public final static String IMAGE_UPLOAD_URL =
 			"http://onehard.fewoo.net/fseditor/image_control.php";		// ̹ ε url
 	public final static String RESPONSE_UPLOAD_URL =					// ̹ ε  url
 			"http://club.fewoo.net/dramaworld/data";
 	public final static int MAX_UPLOAD_SIZE = 737280;					// ִ  ũ
 	public final static String NO_PARTNER =
 			"http://webimage.tutudisk.com/icon/icon_off.gif";			//   ̹
 	public final static String MY_TAG = "[ٵ¯]";
 	public final static String TAIL_TAG =								//  ũ
 			"<img border=0 src='http://club.fewoo.net/dramaworld/data/__132838044794385.gif'" +
 			" onload='javascript:if(this.width>600) this.width=600;'><BR>";
 	public final static String CHECK_ID_URL =							// tutu id check url
 			"http://ddononi.cafe24.com/tutu/tutu.php";
 	
 	public TuTuSweeper(){
 	}
 
 	/**
 	 *     丮 ش.
 	 * 丮 ش.
 	 */
 	private void makeDir() {
 		Calendar cal = Calendar.getInstance();
 		int year = cal.get(Calendar.YEAR);
 		int month = cal.get(Calendar.MONTH) + 1;
 		int day = cal.get(Calendar.DAY_OF_MONTH);
 		StringBuilder sb = new StringBuilder("D:\\tmp");
 		sb.append(File.separator);
 		/*
 		sb.append(year);
 		sb.append(File.separator);
 		sb.append(month +"");
 		sb.append(File.separator);
 		sb.append(day + "");
 		sb.append(File.separator);
 		*/
 		sb.append(getTitle());	//   
 		sb.append(File.separator);
 		File file = new File(sb.toString());
 		if( file.mkdirs() ){
 			System.out.println(file.toString() + "  ϴ.");
 		}
 		//  
 		dir = file.getAbsolutePath();
 	}
 
 	/**
 	 * content  Ϸ Ѵ.
 	 * @param source
 	 * 	content 
 	 * @return
 	 * 	忩
 	 */
 	private boolean doSaveFile(final String source) {
 		File sourcefile = new File(this.dir, SAVE_FILE_NAME);
 		BufferedWriter bw = null;
 		boolean flag = true;
 		try {
 			bw = new BufferedWriter(new FileWriter(sourcefile));
 			bw.write(source);
 			bw.flush();
 			System.out.println("ҽ ߽ϴ.");
 		} catch (IOException e) {
 			flag = false;
 			e.printStackTrace();
 		}finally{
 			try {
 				bw.close();
 			} catch (IOException e) {}
 		}
 
 		return flag;
 	}
 
 	/**
 	 * ־ url ش urlҽ ´.
 	 * @return url source
 	 */
 	protected String downloadSource(){
 		URL url = null;
 		InputStream is = null;
 		StringBuilder sb = new StringBuilder();
 		String line;
 		BufferedReader br = null;
 		try {
 			url = new URL(TUTU_URL+ id);
 			 URLConnection conn = url.openConnection();
 			 is = conn.getInputStream();
 			 br = new BufferedReader(new InputStreamReader(is));
 			 while((line = br.readLine()) != null) {
 				sb.append(line).append("\r\n");
 			 }
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}finally{
 			try {
 				br.close();
 				is.close();
 			} catch (IOException e) {}
 		}
 
 		return sb.toString();
 	}
 
 	/**
 	 * content  ̹ url ÿ ̹Ϸ Ѵ.
 	 * @param source
 	 *		image url
 	 * @return
 	 * 		 File ü
 	 */
 	private File saveToImageFile(final String source){
 		File filename = new File(source);
 		FileOutputStream imageFile = null;
 		BufferedInputStream bis = null;
 		File file = new File(this.dir, filename.getName());
 		try {
 			imageFile = new FileOutputStream(file);
 			URL url = new URL(source);
 			bis = new BufferedInputStream(url.openStream());
 			int size;
 			while( (size = bis.read()) > -1){
 				imageFile.write(size);
 			}
 			imageFile.flush();
 			if(file.length() <=0){	// ũⰡ 0ϰ
 				System.out.println("̹  !");
 				return null;
 			}
 			System.out.println(filename.getName() + " ̹  Ϸ");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}finally{
 			try {
 				imageFile.close();
 				bis.close();
 			} catch (IOException e) {}
 		}
 		return file;
 	}
 
 	/**
 	 *  ̺귯 ̿Ͽ content url html
 	 * Ľ   ´.	html ͸
 	 *      ִ.
 	 * @return
 	 * 	 
 	 */
 	private String getTitle(){
 		String title = null;
 		try {
 			Source source = new Source(new URL(TUTU_URL+ this.id));
 			source.fullSequentialParse();
 			List<Element> rootList = source.getAllElements(HTMLElementName.TD);
 			String width;
 			for(Element elem : rootList){
 				width = elem.getAttributeValue("width");
 				if(width == null) {
 					continue;
 				}else if(width.contains("605")){
 					// ŸƲ  ¿ 
 					title = elem.getAttributeValue("title").toString().trim();
 					//System.out.println(" : " + title);
 				}
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return MY_TAG + title/*.replace(".", "")*/.replace("/", "").replace(":", "_");
 	}
 	
 	private TuTuExtraInfo getCategory(){
 		TuTuExtraInfo info = new TuTuExtraInfo();
 		info.setTitle(this.getTitle());
 		try {
 			Source source = new Source(new URL(TUTU_URL+ this.id));
 			source.fullSequentialParse();
 			List<Element> rootList = source.getAllElements(HTMLElementName.TD);
 			String width, bgcolor;
 			int index = 0;
 			for(Element elem : rootList){
 				width = elem.getAttributeValue("width");
 				bgcolor = elem.getAttributeValue("bgcolor");
 				if(width == null || bgcolor == null) {
 					continue;
 				}else if(width.contains("135") && bgcolor.contains("#FFFFFF")){
 					Segment seg;
 					String tmpStr;
 					if(index == 0){
 						// ̵ ü  ʿ....
 					}else if(index == 1){
 						seg = elem.getContent();
 						tmpStr = seg.toString();
 						String size = null;
 						try{
 							size = tmpStr.split("<span class=\"gc\">/</span>")[1].trim();
 							info.setSize(size);
 						}catch(Exception e){
 							return null;
 						}
 						System.out.println(" : " + size);
 					}else if(index == 2){
 						seg = elem.getContent();
 						tmpStr = seg.toString();
 						String category = tmpStr.replace("&nbsp;", " ").trim();
 						System.out.println("з : " + category);
 						info.setCategory(category);
 					}
 					index++;
 				}
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return info;
 	}	
 
 	public void getStartContent(final String id){
 		this.id = id;
 		TuTuExtraInfo info = getCategory();
 		
 		makeDir();	// 丮 
 		String contentHtml = "";
 		try {
 			Source source = new Source(new URL(TUTU_URL + id));
 			source.fullSequentialParse();
 			// td node ̱
 			List<Element> tdList = source.getAllElements(HTMLElementName.TD);
 			for(Element elem : tdList){
 				String style = elem.getAttributeValue("style");
 				if(style == null) {
 					continue;
 				}else if(style.contains("word-break:break-all;font-size:9pt")
 						&& elem.getAttributeValue("valign").contains("top")
 						&& elem.getAttributeValue("align").contains("center")){	// ε κ 
 					String contents = elem.getContent().toString().trim();
 					// ̹  븸 
 					contents = contents.replaceAll("<IMG ([^>]+)>", "");
 					List<Element> imageList = elem.getAllElements(HTMLElementName.IMG);	// ̹ 
 					String uploadTag = "";
 					
 					//	̹ tag 鼭 src Ӽ ̹url ̾ ´.
 					//	 ̹ ּҸ ã Ϸ     ũ⺸ ũ
 					//	BufferedImage ̿Ͽ ̹ ũ⸦  ϵ弭 εѴ.
 					//	ϵ  ε尡 ϷǸ ε ̹tag ̾ƿ Ʈ ̰
 					//	tail_tag ޾ ؽƮ Ϸ Ѵ.
 					for(Element subElem : imageList){
 						String src = subElem.getAttributeValue("src");
 						if(src == null) {
 							continue;
 						}
 						// ε ̹ 
 						System.out.println(src + " ãҽϴ.");
 						File file = saveToImageFile(src);
 						if(file != null){	//    Ǿ 츸
 							System.out.println("̹ ũ : " + file.length() +"bytes");
 							// ϻ ̱       750lkb 
 							while(file.length() > MAX_UPLOAD_SIZE ){
 								BufferedImage originalImage = ImageIO.read(file);
 								int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
 
 								BufferedImage resizeImageJpg = resizeImage(originalImage, type);
 								ImageIO.write(resizeImageJpg, "jpg", file);
 							}
 
 							uploadTag += uploadImage(file);
 						}
 					}
 					contentHtml = uploadTag + contents + TAIL_TAG;	//  
 					doSaveFile(contentHtml);	//  
 				}
 			}
 			info.setContent(contentHtml);
 			// ID   ߺüũ
 			if( !registerToDB(id, info) ){
 				System.out.println(id + " ߺ Դϴ.");
 				//return;
 			}			
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void getStartMultiContents(final String url){
 		//ArrayList<Content> contentsList = new ArrayList<Content>();
 		try {
 			Source source = new Source(new URL(url));
 			source.fullSequentialParse();
 			List<Element> divList = source.getAllElements(HTMLElementName.DIV);
 			for(Element elem : divList){
 				String style = elem.getAttributeValue("style");
 				if(style == null) {
 					continue;
 				}else if(style.contains("visibility:hidden;z-index:9999;position:absolute;width=100%;background-color:white;top:6px")){	// ε κ 
 					List<Element> imageList = elem.getAllElements(HTMLElementName.IMG);	// ̹ 
 					for(Element subElem : imageList){
 						String src = subElem.getAttributeValue("src");
 						if(src == null) {
 							continue;
 						}else if(src.contains(NO_PARTNER)){
 							//   ƴѰ츸
 							String id = elem.getAttributeValue("id").replace("full_title_", "");
 							getStartContent(id);
 						}
 					}
 				}
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 *  ̹  ϵ  ε Ѵ.
 	 * @param file
 	 *		 ̹ 
 	 * @return
 	 * 		  ε ̹ ± ex:) <img src='some_image.jpg'>
 	 */
 	private String uploadImage(final File file){
 		String tag = "";	//  ±
         MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
         FileBody encFile = new FileBody(file);	// ε ̹  
 
         /*
          *
          *  FORM 
          *  <input type="hidden" name="pg_mode" value="insert">
          *	<input type="hidden" name="img_width">
          *	<input type="hidden" name="img_height">
          *	<input type="hidden" name="limit_width" value="">
          *	<input type="hidden" name="limit_height" value="">
          *	<input type="hidden" name="get_mode" value="0">
          *	<input type="hidden" name="save_path" value="dramaworld/data">
          *  <input type="hidden" name="exten_clubid" value="dramaworld">
          *  <input type="file" name="targetfile" onchange="ShowImg(this, 'preView') />
          */
 
         // input  ֱ
         entity.addPart("targetfile", encFile);
         try {
             entity.addPart("pg_mode", new StringBody("insert"));
             entity.addPart("save_path", new StringBody("dramaworld/data"));
             entity.addPart("exten_clubid", new StringBody("dramaworld"));
         } catch (UnsupportedEncodingException e) {
         	System.out.println("error");
         }
 
         // post  
         HttpPost request = new HttpPost(IMAGE_UPLOAD_URL);
         request.setEntity(entity);
         HttpClient client = new DefaultHttpClient();
 
         try {
             BasicResponseHandler responseHandler = new BasicResponseHandler();
             String responseBody = client.execute(request, responseHandler);
 
             //  Ͻ ε ̹ url ãƼ img tag  
             // content Ͽ ־־ ߿ content  Ͽ ٿֱ
             // Ҽ ֵ .
             if (responseBody != null && responseBody.length() > 0
             		&& responseBody.contains(RESPONSE_UPLOAD_URL)) {
             	try{
 		       		String[] arr = responseBody.split("//");
 		    		String src = arr[1].split("(;)")[0];
 		    	    tag = "<img src='http://" + src.replace(")", "").replace("\"", "")
 		    				 +"' border='0' onLoad='javascript:"
 		    				 +"if(this.width>600) this.width=600;'><br>";
 		    				 
 		    	    System.out.println(tag);
             	}catch(IndexOutOfBoundsException ibe){
             		 System.out.println(responseBody);
             	}
 //            	     ɼ .
 //               Pattern pattern;
 //               Matcher matcher;
 //               pattern = Pattern.compile("http:+;");
 //               matcher = pattern.matcher(responseBody);
 //               if(matcher.matches()) {
 //               	 System.out.println("find"); // matcher Ī  ü ȯ
 //                   System.out.println(matcher.group()); // matcher Ī  ü ȯ
 //               }
             }else{
             	 System.out.println("************** ~~~~!!");
             	 System.out.println(responseBody);
             }
         } catch (ClientProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return tag;
 	}
 	
 	/**
 	 *    ̵ üũѴ.
 	 *  ̵, , īװ, Ʈ  ũ⸦
 	 *   DB ̵ üũ ȯ
  	 * @param id
  	 * 	˻ ̵
 	 * @return
 	 * 	̵  false otherwise true
 	 */
 	static public boolean registerToDB(final String id, final TuTuExtraInfo info){
 		if(info == null){
 			return false;
 		}
         // post  
         Vector<NameValuePair> vars = new Vector<NameValuePair>();
         vars.add(new BasicNameValuePair("idx", id));
         vars.add(new BasicNameValuePair("title", info.getTitle() ));
         vars.add(new BasicNameValuePair("content", info.getContent() ));
         vars.add(new BasicNameValuePair("category", info.getCategory() ));
         vars.add(new BasicNameValuePair("size", info.getSize() ));
         
         HttpPost request = new HttpPost(CHECK_ID_URL);
         HttpClient client = new DefaultHttpClient();
         try {
         	request.setEntity(new UrlEncodedFormEntity(vars, "utf-8"));
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             String responseBody = client.execute(request, responseHandler);
 
             //  Ͻ ε ̹ url ãƼ img tag  
             // content Ͽ ־־ ߿ content  Ͽ ٿֱ
             // Ҽ ֵ .
             if (responseBody.equalsIgnoreCase("empty")) {	// ID ٸ true
             	return true;
             }else{
             	 System.out.println(responseBody);
             }
         } catch (ClientProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
 		return false;
 		
 	}
 
 	/**
 	 * ̹  ϱ
 	 * @param originalImage
 	 *	 ̹
 	 * @param type
 	 *	̹ Ÿ
 	 * @return
 	 * 		 BufferedImage
 	 */
 	private static BufferedImage resizeImage(final BufferedImage originalImage, final int type){
 		int width = (int) (originalImage.getWidth() * .8);
 		int height = (int) (originalImage.getHeight() * .8);
 		BufferedImage resizedImage = new BufferedImage(width, height, type);
 		/*
 		Graphics2D g = resizedImage.createGraphics();
 		g.drawImage(originalImage, 0, 0, width, height, null);
 		g.dispose();
 		*/
 		return resizedImage;
 	}
 }

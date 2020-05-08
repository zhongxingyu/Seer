 package com.jfboily.fxgea;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.util.Log;
 
 public class TileMap 
 {
 	private int mapWidth;
 	private int mapHeight;
 	private int tileWidth;
 	private int tileHeight;
 	private Tile[][] layers;
 	
 	private int nbLayers;
 	private Bitmap bitmapTileset;
 	
 	public void load(String tilEDfilename)
 	{
 		try
 		{
 			InputStream is = Game.getGame().getAssets().open(tilEDfilename);
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			factory.setValidating(false);
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			builder.setEntityResolver(new EntityResolver() {
 				
				@Override
 				public InputSource resolveEntity(String publicId, String systemId)
 						throws SAXException, IOException {
 					// TODO Auto-generated method stub
 					return new InputSource(new ByteArrayInputStream(new byte[0]));
 				}
 			});
 			
 			Document doc = builder.parse(is);
 			
 			// le docElement = tag 'map'
 			Element docElement = doc.getDocumentElement();
 			
 			// verifier que le data est pour une map orthogonale
 			if(!docElement.getAttribute("orientation").equals("orthogonal"))
 			{
 				Log.e("Screen:createStaticTiledBG", "Le tileset N'EST PAS ORTHOGONAL!!");
 				throw new IllegalArgumentException("TILESET is not orthogonal");
 			}
 			
 			// attributs de la map et des tiles
 			mapWidth = Integer.parseInt(docElement.getAttribute("width"));
 			mapHeight = Integer.parseInt(docElement.getAttribute("height"));
 			tileWidth = Integer.parseInt(docElement.getAttribute("tilewidth"));
 			tileHeight = Integer.parseInt(docElement.getAttribute("tileheight"));
 			
 			// load le tileset
 			NodeList tilesetNodes = docElement.getElementsByTagName("tileset");
 			
 			// supporte seulement le premier tileset... ignore les autres!!!
 			Element current = (Element)tilesetNodes.item(0);
 			NodeList imageNodes = current.getElementsByTagName("image");
 			String fnameTileset = ((Element)imageNodes.item(0)).getAttribute("source");
 			
 			// charge l'image
 			InputStream is2 = Game.getGame().getAssets().open(fnameTileset);
 			bitmapTileset = BitmapFactory.decodeStream(is2);
 			
 			// charge les layers
 			NodeList layerNodes = docElement.getElementsByTagName("layer");
 			nbLayers = layerNodes.getLength();
 			layers = new Tile[nbLayers][];
 			for(int i = 0; i < nbLayers; i++)
 			{
 				int mapx=0, mapy=0;
 				int screenx=0, screeny=0;
 				Element layer = (Element)layerNodes.item(i);
 				layers[i] = new Tile[mapWidth * mapHeight];
 				// premier tag data = le data!
 				Element data = (Element)layer.getElementsByTagName("data").item(0);
 				NodeList tileNodes = data.getElementsByTagName("tile");
 				for(int j = 0; j < tileNodes.getLength(); j++)
 				{
 					Element tile = (Element)tileNodes.item(j);
 					
 					Rect tileRect = new Rect();
 					tileRect.left = mapx * tileWidth;
 					tileRect.top = mapy * tileHeight;
 					tileRect.right = tileRect.left + tileWidth;
 					tileRect.bottom = tileRect.top + tileHeight;
 					layers[i][j] = new Tile(Integer.parseInt(tile.getAttribute("gid")), mapx, mapy, tileRect);
 					mapx++;
 					if(mapx >= mapWidth)
 					{
 						mapx = 0;
 						mapy++;
 					}
 				}
 			}
 			
 			
 		}
 		catch (Exception e)
 		{
 			Log.e("Screen:createStaticTiledBG", "Exception au parse du fichier XML ("+e.getMessage()+")");
 		}
 	}
 	
 	public Bitmap renderAllLayers()
 	{
 		Bitmap bitmap = Bitmap.createBitmap(mapWidth * tileWidth, mapHeight * tileHeight, Bitmap.Config.ARGB_8888);
 		Canvas canvas = new Canvas(bitmap);
 		Rect src = new Rect();
 		Rect dst = new Rect();
 		Tile tile;
 		int nbTilesW = bitmapTileset.getWidth() / tileWidth;
 		int nbTilesH = bitmapTileset.getHeight() / tileHeight;
 		for(int i = 0; i < nbLayers; i++)
 		{
 			for(int t = 0; t < layers[i].length; t++)
 			{
 				tile = layers[i][t];
 				int tileIndex = tile.type - 1;
 				src.top = (tileIndex / nbTilesW) * tileHeight;
 				src.left = (tileIndex % nbTilesW) * tileWidth;
 				src.right = src.left + tileWidth;
 				src.bottom = src.top + tileHeight;
 				dst = tile.screenRect;
 				canvas.drawBitmap(bitmapTileset, src, dst, null);
 			}
 		}
 		return bitmap;
 	}
 	
 	public Bitmap renderLayer(int layer)
 	{
 		return null;
 	}
 	
 	public Tile getTileMap(int mapX, int mapY, int layer)
 	{
 		return layers[layer][mapY*mapWidth+mapX];
 	}
 	
 	public Tile getTileScreen(int screenX, int screenY, int layer)
 	{
 		int mapX = screenX / tileWidth;
 		int mapY = screenY / tileHeight;
 		return layers[layer][mapY*mapWidth+mapX];
 	}
 	
 	public int getTileWidth()
 	{
 		return tileWidth;
 	}
 	
 	public int getTileHeight()
 	{
 		return tileHeight;
 	}
 	
 	
 	public class Tile
 	{
 		public int type;
 		public int state = 0;
 		public int mapX;
 		public int mapY;
 		public Rect screenRect;
 		
 		public Tile(int type, int mapX, int mapY, Rect screenRect)
 		{
 			this.type = type;
 			this.mapX = mapX;
 			this.mapY = mapY;
 			this.screenRect = screenRect;
 		}
 	}
 }

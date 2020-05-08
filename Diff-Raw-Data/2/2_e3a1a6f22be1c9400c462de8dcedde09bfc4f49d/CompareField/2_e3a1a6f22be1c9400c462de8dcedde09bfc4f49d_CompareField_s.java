 package net.mytcg.topcar.ui.custom;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 
 import net.mytcg.topcar.http.ConnectionGet;
 import net.mytcg.topcar.util.Const;
 import net.mytcg.topcar.util.SettingsBean;
 import net.rim.device.api.math.Fixed32;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.system.EncodedImage;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Graphics;
 
 public final class CompareField extends Field {
 	
 	private String url;
 	private String file;
 	private Bitmap image;
 	
 	public CompareField(String url) {
 		this.url = url;
 		if ((url != null)&&(url.length() > 0)){
 			file = url.substring(url.indexOf(Const.cards)+Const.cards_length, url.indexOf(Const.png));
 		}
 		construct();
 	}
 	
 	protected void drawFocus(Graphics g, boolean x) {
 		
 	}
 	public void setUrl(String url) {
 		this.url = url;
 		if ((url != null)&&(url.length() > 0)){
 			file = url.substring(url.indexOf(Const.cards)+Const.cards_length, url.indexOf(Const.png));
 		}
 		construct();
 	}
 	
 	public synchronized void saveData(byte[] data, String filename) {
 		FileConnection _file = null;
 		OutputStream output = null;
 		try {
 			_file = (FileConnection)Connector.open(Const.getStorage()+Const.PREFIX+filename);
 			_file.create();
 			output = _file.openOutputStream();
 			output.write(data);
 			output.close();
 			_file.close();
 		} catch (Exception e) {}
 	}
 	public void getData() {
 		FileConnection _file = null;
 		InputStream input = null;
 		try {
 			_file = (FileConnection)Connector.open(Const.getStorage()+Const.PREFIX+file);
 			if (!_file.exists()) {
 				_file.close();
 				doConnect(url);
 			} else {
 				input = _file.openInputStream();
 				byte[] data = new byte[(int) _file.fileSize()];
 				input.read(data);
 				input.close();
 				_file.close();
 				image = (EncodedImage.createEncodedImage(data, 0, data.length)).getBitmap();
 				landscape();
 				invalidate();
 			}
 		} catch (Exception e) {}
 	}
 	
 	public void construct() {
 		int font = Const.FONT;
 		image = Const.getThumbLoading();
 		FileConnection _file = null;
 		InputStream input = null;
 		try {
 			SettingsBean _instance = SettingsBean.getSettings();
 			_file = (FileConnection)Connector.open(Const.getStorage()+Const.PREFIX+_instance.loadingflip);
 			input = _file.openInputStream();
 			byte[] data = new byte[(int) _file.fileSize()];
 			input.read(data);
 			input.close();
 			_file.close();
 			image = (EncodedImage.createEncodedImage(data, 0, data.length)).getBitmap();
 		} catch (Exception e) {}
 		if (file != null) {
 			getData();
 		}
 		landscape();
 		if (file != null) {
 			getData();
 		}
 		Font _font = getFont();
 		_font = _font.derive(Const.TYPE,font);
 		setFont(_font);
 	}
 	
 	public void onUndisplay() {
 		
 	}
 	public void onUnfocus() {
 		//invalidate();
 		
 	}
 	public void onVisibilityChange(boolean visible) {
 		
 	}
 	public void focusRemove() {
 		
 	}
 	
 	public int getPreferredWidth() {
 		
 		if ((Const.getPortrait())) {
 			return Const.getWidth();
 		}else return Const.getWidth()/2;
 	}
 	public int getPreferredHeight() {
 		if ((Const.getPortrait())) {
			return (Const.getHeight()-Const.getButtonCentre().getHeight());
 		}else
 		return (Const.getHeight()-Const.getButtonCentre().getHeight());
 	}
 	protected void layout(int width, int height) {
 		setExtent(getPreferredWidth(),getPreferredHeight());
     }
 	public boolean isFocusable() {
     	return true;
     }
 	public void paint(Graphics g) {
 		int _xPts[] = {0,0,getPreferredWidth(),getPreferredWidth()};
 		int _yPts[] = {0,getPreferredHeight(),getPreferredHeight(),0};
 		g.drawTexturedPath(_xPts,_yPts,null,null,0,0,Fixed32.ONE,0,0,Fixed32.ONE,Const.getBackground());
 		g.drawBitmap(5, 5, image.getWidth(), image.getHeight(), image, 0, 0);
 	}
 	protected boolean navigationClick(int status, int time) {
         fieldChangeNotify(1);
         return true;
     }
 	
 	ConnectionGet cG;
 	int timeout = 0;
 	
 	public void doConnect(String url) {
 		if ((url != null)&&(url.length() > 0)) {
 			(Const.getConnection()).addConnect(url, file, this);
 		}
 	}
 	public void landscape() {
 		if ((Const.getPortrait())) {
 			try {
 				image = Const.rotate(image, 180);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 		}else {
 			try {
 				image = Const.rotate(image, 270);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	public void process(byte[] data, String filename) {
 		image = (EncodedImage.createEncodedImage(data, 0, data.length)).getBitmap();
 		landscape();
 		invalidate();
 		saveData(data, filename);
 	}
 }

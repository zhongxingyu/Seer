 package net.mytcg.dev.ui.custom;
 
 import java.io.InputStream;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 
 import net.mytcg.dev.util.Auction;
 import net.mytcg.dev.util.Card;
 import net.mytcg.dev.util.Const;
 import net.mytcg.dev.util.Product;
 import net.rim.device.api.math.Fixed32;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.system.EncodedImage;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Graphics;
 
 public final class ThumbnailField extends Field {
 	
 	private String thumbfile;
 	private String frontfile;
 	private String backfile;
 	
 	private boolean focus;
 	private boolean focusable = true;
 	private String label1;
 	private String label2;
 	private String label3;
 	
 	private Bitmap button_centre;
 	
 	private Bitmap button_sel_centre;
 	
 	private Bitmap button_thumbnail;
 	
 	private Bitmap note;
 	
 	private Card card = null;
 	
 	private Product product = null;
 	
 	private Auction auction = null;
 	
 	private boolean delete = false;
 	
 	public int getId() {
 		if(card != null){
 			return card.getId();
 		}else if(product != null){
 			return product.getId();
 		}else if(auction != null){
 			return auction.getId();
 		}else return -1;
 	}
 	public String getDescription() {
 		if(card != null){
 			label2 = card.getQuality();
 			label3 = "Rating: " + card.getRating();
 			return card.getDesc();
 		}else if(product != null){
 			return product.getDesc();
 		}else if(auction != null){
 			return auction.getDesc();
 		}else return "";
 	}
 	public String getType() {
 		return product.getType();
 	}
 	public String getNumCards() {
 		return product.getNumCards();
 	}
 	public int getQuantity() {
 		if(card != null){
 			return card.getQuantity();
 		}else if(product != null){
 			return product.getQuantity();
 		}else if(auction != null){
 			return 1;
 		}else return 0;
 	}
 	public String getThumbUrl() {
 		if(card != null){
 			return card.getThumburl();
 		}else if (product != null){
 			return product.getThumburl();
 		}else if (auction != null){
 			return auction.getThumburl();
 		}else return "";
 	}
 	public String getFrontUrl() {
 		if(card != null){
 			return card.getFronturl();
 		}else if (product != null){
 			return product.getFronturl();
 		}else return "";
 	}
 	public String getBackUrl() {
 		if(card != null){
 			return card.getBackurl();
 		}else if (product != null){
 			return product.getBackurl();
 		}else return "";
 	}
 	public String getThumbFile() {
 		return thumbfile;
 	}
 	public String getFrontFile() {
 		return frontfile;
 	}
 	public String getBackFile() {
 		return backfile;
 	}
 	public Card getCard() {
 		return card;
 	}
 	public Product getProduct() {
 		return product;
 	}
 	public Auction getAuction() {
 		return auction;
 	}
 	
 	public ThumbnailField(Card card) {
 		this(card, false);
 	}
 	public ThumbnailField(Card card, boolean delete) {
 		this.card = card;
 		this.delete = delete;
 		this.label2 = "";
 		this.label3 = "";
 		
 		/*if (card.getUpdated() == 1) {
 			this.delete = true;
 		}*/
 		
 		if ((getThumbUrl() != null)&&(getThumbUrl().length() > 0)){
 			thumbfile = getThumbUrl().substring(getThumbUrl().indexOf(Const.cards)+Const.cards_length, getThumbUrl().indexOf(Const.jpeg));
 		}
 		if ((getFrontUrl() != null)&&(getFrontUrl().length() > 0)){
 			frontfile = getFrontUrl().substring(getFrontUrl().indexOf(Const.cards)+Const.cards_length, getFrontUrl().indexOf(Const.jpeg));
 		}
 		if ((getBackUrl() != null)&&(getBackUrl().length() > 0)){
 			backfile  = getBackUrl().substring(getBackUrl().indexOf(Const.cards)+Const.cards_length, getBackUrl().indexOf(Const.jpeg));
 		}
 		construct(getDescription());
 	}
 	public ThumbnailField(Product product){
 		this.product = product;
 		this.delete = false;
 		this.label2 = "";
 		this.label3 = "";
 		if ((getThumbUrl() != null)&&(getThumbUrl().length() > 0)){
 			thumbfile = getThumbUrl().substring(getThumbUrl().indexOf(Const.products)+Const.products_length, getThumbUrl().indexOf(Const.png));
 		}
 		construct(getDescription());
 	}
 	public ThumbnailField(Auction auction){
 		this.auction = auction;
 		this.delete = false;
 		this.label2 = "";
 		this.label3 = "";
 		if ((getThumbUrl() != null)&&(getThumbUrl().length() > 0)){
 			thumbfile = getThumbUrl().substring(getThumbUrl().indexOf(Const.cards)+Const.cards_length, getThumbUrl().indexOf(Const.jpeg));
 		}
 		construct(getDescription());
 	}
 	
 	protected void drawFocus(Graphics g, boolean x) {
 		
 	}
 	public void getData(int type) {
 		FileConnection _file = null;
 		InputStream input = null;
 		String filename = "";
 		String url = "";
 		switch (type) {
 		case 0:
 			filename = thumbfile;
 			url = getThumbUrl();
 			break;
 		case 1:
 			filename = frontfile;
 			url = getFrontUrl();
 			break;
 		case 2:
 			filename = backfile;
 			url = getBackUrl();
 			break;
 		}
 		try {
 			_file = (FileConnection)Connector.open(Const.getStorage());
 			if (!_file.exists()) {
 				_file.mkdir();
 			}
 			_file.close();
 			_file = (FileConnection)Connector.open(Const.getStorage()+Const.PREFIX+filename);
 			if (!_file.exists()) {
 				_file.close();
 				_file = null;
 				doConnect(url, type);
 			} else if (_file.fileSize() == 0) {
 				_file.delete();
 				_file.close();
 				_file = null;
 				doConnect(url, type);
 			} else if (delete) {
 				_file.delete();
 				_file.close();
 				_file = null;
 				doConnect(url, type);
 			} else if (!delete) {
 				if (type==0) {
 					input = _file.openInputStream();
 					byte[] data = new byte[(int) _file.fileSize()];
 					input.read(data);
 					input.close();
 					_file.close();
 					button_thumbnail = Const.getSizeImage(EncodedImage.createEncodedImage(data, 0, data.length));
 					invalidate();
 				}
 			}
 		} catch (Exception e) {
 			try {
 				_file.close();
 			} catch (Exception ex) {}
 			doConnect(url, type);
 		}
 	}
 	
 	public void construct(String label) {
 		int font = Const.FONT;
 		
 		button_centre = Const.getThumbCentre();
 		button_sel_centre = Const.getThumbRightEdge();
 		note = Const.getNote();
 		
 		if (getQuantity() == 0) {
 			button_thumbnail = Const.getEmptyThumb();
 		} else {
 			button_thumbnail = Const.getThumbLoading();
 		}
 		if ((thumbfile != null)&&(getQuantity()>0)) {
 			getData(0);
 		}
 		Font _font = getFont();
 		_font = _font.derive(Const.TYPE,font);
 		setFont(_font);
 		this.label1 = label;
 	}
 	public void setLabel(String label) {
 		this.label1 = label;
 		invalidate();
 	}
 	
 	public void setSecondLabel(String label) {
 		this.label2 = label;
 		invalidate();
 	}
 	
 	public void setThirdLabel(String label) {
 		this.label3 = label;
 		invalidate();
 	}
 	
 	public void setProductType(String type) {
 		this.product.setType(type);
 	}
 	
 	public void setProductNumCards(String num) {
 		this.product.setNumCards(num);
 	}
 	
 	public void setProductPrice(String price) {
 		this.product.setPrice(price);
 	}
 	
 	public Bitmap getThumbnail(){
 		return button_thumbnail;
 	}
 	public void setThumbnail(Bitmap thumb){
 		this.button_thumbnail = thumb;
 	}
 	public int getPreferredWidth() {
 		return Const.getWidth();
 	}
 	public int getPreferredHeight() {
 		return button_sel_centre.getHeight();
 	}
 	protected void layout(int width, int height) {
 		setExtent(getPreferredWidth(),getPreferredHeight());
     }
 	public boolean isFocusable() {
     	return focusable;
     }
 	public void setFocusable(boolean focusable){
 		this.focusable = focusable;
 	}
 	public void paint(Graphics g) {
 		//int _xPts[] = {0,0,getPreferredWidth(),getPreferredWidth()};
 		//int _yPts[] = {0,Const.getHeight(),Const.getHeight(),0};
 		//g.drawTexturedPath(_xPts,_yPts,null,null,0,0,Fixed32.ONE,0,0,Fixed32.ONE,Const.getBackground());
 		
 		int xPts2[] = {0,0,getPreferredWidth(),getPreferredWidth()};
 		int yPts2[] = {0,getPreferredHeight(),getPreferredHeight(),0};
 		
 		g.setColor(Const.FONTCOLOR);
 		
 		if (focus) {
 			g.setColor(Const.SELECTEDCOLOR);
 			g.drawTexturedPath(xPts2,yPts2,null,null,0,0,Fixed32.ONE,0,0,Fixed32.ONE,button_centre);
 			g.drawBitmap(Const.getWidth() - (button_sel_centre.getWidth() + 5), 0, button_sel_centre.getWidth(), getPreferredHeight(), button_sel_centre, 0, 0);
 		} else {
 			//int xPts1[] = {2,2,getPreferredWidth()-2,getPreferredWidth()-2};
 			//int yPts1[] = {0,getPreferredHeight(),getPreferredHeight(),0};
 				
 			g.drawTexturedPath(xPts2,yPts2,null,null,0,0,Fixed32.ONE,0,0,Fixed32.ONE,button_centre);
 		}
 		
 		//int xPts[] = {0,0,getPreferredWidth(),getPreferredWidth()};
 		//int yPts[] = {0,Const.getHeight(),Const.getHeight(),0};
 		//g.drawTexturedPath(xPts,yPts,null,null,0,0,Fixed32.ONE,0,0,Fixed32.ONE,Const.getBackground());
 		
 		
 		
		g.drawBitmap(5, 3, button_thumbnail.getWidth(), getPreferredHeight(), button_thumbnail, 0, 0);
 		
 		Font _font = getFont();
 		_font = _font.derive(Const.TYPE,Const.FONT+2);
 		g.setFont(_font);
 		if(card != null){
 			if ((card.getNote() != null)&&(card.getNote().length() > 0)) {
 				g.drawBitmap(5, 5, note.getWidth(), note.getHeight(), note, 0, 0);
 			}
 			
 			if (card.getUpdated() == 1) {
 				g.drawText("*" +label1 +(card.getQuantity()>-1?" ("+card.getQuantity()+")":""), button_thumbnail.getWidth()+10, 4);
 			} else {
 				g.drawText(label1 +(card.getQuantity()>-1?" ("+card.getQuantity()+")":""), button_thumbnail.getWidth()+10, 4);
 			}
 		}else if(product != null){
 			g.drawText(label1, button_thumbnail.getWidth()+10, 4);
 		}else if(auction != null){
 			g.drawText(label1, button_thumbnail.getWidth()+10, 4);
 		}
 		if(!label2.equals("")){
 			g.drawText(label2, button_thumbnail.getWidth()+10, Const.FONT+6);
 		}
 		if(!label3.equals("")){
 			g.drawText(label3, button_thumbnail.getWidth()+10, (Const.FONT*2)+8);
 		}
 		
 		_font = _font.derive(Font.PLAIN,Const.FONT);
 		g.setFont(_font);
 	}
 	protected void onFocus(int direction) {
 		focus = true;
 		invalidate();
 	}
 	protected void onUnfocus() {
 		focus = false;
 		invalidate();
 	}
 	protected boolean navigationClick(int status, int time) {
         fieldChangeNotify(1);
         return true;
     }
 	
 	public void doConnect(String url, int type) {
 		
 		if ((url != null)&&(url.length() > 0)) {
 			(Const.getConnection()).addConnect(url, type, this);
 		}
 	}
 	public void process(byte[] data, int type) {
 		if (type == 0) {
 			try{
 				button_thumbnail = Const.getSizeImage(EncodedImage.createEncodedImage(data, 0, data.length));
 			}catch(IllegalArgumentException e){
 				
 			}
 			invalidate();
 		}
 		if (type < 2) 
 			getData(++type);
 	}
 }

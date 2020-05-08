 package org.accesointeligente.client.widgets;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.ScriptElement;
 import com.google.gwt.i18n.client.LocaleInfo;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.*;
 
 public class ShareThis extends Composite {
 	private static ShareThisUiBinder uiBinder = GWT.create(ShareThisUiBinder.class);
 	interface ShareThisUiBinder extends UiBinder<Widget, ShareThis> {}
 
 	public enum Align {
 		VERTICAL("vertical", "tall", "box_count"),
 		HORIZONTAL("horizontal", "medium", "button_count");
 
 		private String twitterPropertyName;
 		private String googlePlusPropertyName;
 		private String facebookPropertyName;
 
 		private Align(String twitterPropertyName, String googlePlusPropertyName, String facebookPropertyName) {
 			this.twitterPropertyName = twitterPropertyName;
 			this.googlePlusPropertyName = googlePlusPropertyName;
 			this.facebookPropertyName = facebookPropertyName;
 		}
 
 		public String getTwitterPropertyName() {
 			return twitterPropertyName;
 		}
 
 		public String getGooglePlusPropertyName() {
 			return googlePlusPropertyName;
 		}
 
 		public String getFacebookPropertyName() {
 			return facebookPropertyName;
 		}
 	}
 
 	@UiField HTMLPanel shareThisPanel;
 
 	private HTML htmlTwitter;
 	private HTML htmlGooglePlus;
 	private HTML htmlFacebook;
 	private String href = "";
 	private String title = "";
 	private String message = "";
 	private String langLong = "";
 	private String langShort = "";
 	private String via = "";
 	private Align align = Align.VERTICAL;
 	private Panel shareThisContainer;
 
 	public ShareThis() {
 		initWidget(uiBinder.createAndBindUi(this));
 		clear();
 		setLangShort(getLocaleLang(true));
 		setLangLong(getLocaleLang(false));
 	}
 
 	public HTML getHtmlTwitter() {
 		return htmlTwitter;
 	}
 
 	public void setHtmlTwitter(HTML twitter) {
 		this.htmlTwitter = twitter;
 	}
 
 	public HTML getHtmlGooglePlus() {
 		return htmlGooglePlus;
 	}
 
 	public void setHtmlGooglePlus(HTML htmlGooglePlus) {
 		this.htmlGooglePlus = htmlGooglePlus;
 	}
 
 	public HTML getHtmlFacebook() {
 		return htmlFacebook;
 	}
 
 	public void setHtmlFacebook(HTML facebook) {
 		this.htmlFacebook = facebook;
 	}
 
 	public String getHref() {
 		return href;
 	}
 
 	public void setHref(String href) {
 		this.href = href;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	public String getLangLong() {
 		return langLong;
 	}
 
 	public void setLangLong(String langLong) {
 		this.langLong = langLong;
 	}
 
 	public String getLangShort() {
 		return langShort;
 	}
 
 	public void setLangShort(String langShort) {
 		this.langShort = langShort;
 	}
 
 	public String getVia() {
 		return via;
 	}
 
 	public void setVia(String via) {
 		this.via = via;
 	}
 
 	public Align getAlign() {
 		return align;
 	}
 
 	public void setAlign(Align align) {
 		this.align = align;
 	}
 
 	public void setup() {
 		setupTwitterScript();
 		setupGooglePlusScript();
 		setupFacebookScript();
 
 		if (align == Align.VERTICAL) {
 			shareThisContainer = new VerticalPanel();
 		} else {
 			shareThisContainer = new HorizontalPanel();
 		}
 
 		drawTwitterButton();
 		drawGooglePlusButton();
 		drawFacebookButton();
 
 		shareThisPanel.add(shareThisContainer);
 	}
 
 	public void clear() {
 		shareThisPanel.clear();
 	}
 
 	private void drawTwitterButton() {
 		String html = "<a ";
 		html += "href=\"https://twitter.com/share\" ";
 		html += "class=\"twitter-share-button\" ";
 		html += "data-count=\"" + align.getTwitterPropertyName() + "\" ";
 		html += "data-text=\"" + message + "\" ";
 		html += "data-url=\"" + href + "\" ";
 		html += "data-lang=\"" + langShort + "\" ";
 
 		if (via != null && via.length() > 0) {
 			html += "data-via=\"" + via + "\" ";
 		}
 
 		html += ">Tweet</a>";
 		setHtmlTwitter(new HTML(html));
 		shareThisContainer.add(htmlTwitter);
 	}
 
 	private void drawGooglePlusButton() {
 		String html = "<g:plusone ";
 		html += "href=\"" + href + "\" ";
 		html += "size=\"" + align.getGooglePlusPropertyName() + "\">";
 		html += "</g:plusone>";
 
 		setHtmlGooglePlus(new HTML(html));
 		shareThisContainer.add(htmlGooglePlus);
 	}
 
 	private void drawFacebookButton() {
 		String html = "<fb:like ";
 		html += "href=\"" + href + "\" ";
 		html += "layout=\"" + align.getFacebookPropertyName() + "\" ";
 		html += "show_faces=\"false\" ";
 		html += "send=\"true\" ";
		html += "width=\"200\">";
 		html += "</fb:like>";
 
 		setHtmlFacebook(new HTML(html));
 		shareThisContainer.add(htmlFacebook);
 	}
 
 	private void setupTwitterScript() {
 		Document doc = Document.get();
 		ScriptElement script = doc.createScriptElement();
 		script.setSrc("https://platform.twitter.com/widgets.js");
 		script.setType("text/javascript");
 		script.setLang("javascript");
 		doc.getBody().appendChild(script);
 	}
 
 	private void setupGooglePlusScript() {
 		Document doc = Document.get();
 		ScriptElement script = doc.createScriptElement();
 		script.setSrc("https://apis.google.com/js/plusone.js");
 		script.setType("text/javascript");
 		script.setLang("javascript");
 		doc.getBody().appendChild(script);
 	}
 
 	private void setupFacebookScript() {
 		Document doc = Document.get();
 		ScriptElement script = doc.createScriptElement();
 		script.setSrc("https://connect.facebook.net/" + langLong + "/all.js#xfbml=1");
 		script.setType("text/javascript");
 		script.setLang("javascript");
 		doc.getBody().appendChild(script);
 	}
 
 	public String getLocaleLang(Boolean shortName) {
 		LocaleInfo localeInfo = LocaleInfo.getCurrentLocale();
 		String localeName = localeInfo.getLocaleName();
 
 		if (shortName == true) {
 			if (localeName.matches("_")) {
 				localeName = localeName.substring(0, localeName.indexOf("_"));
 			}
 		}
 
 		return localeName;
 	}
 }

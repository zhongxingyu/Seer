 package harris.GiantBomb;
 
 import harris.GiantBomb.GBObject.ObjectType;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.nullwire.trace.ExceptionHandler;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.webkit.WebView;
 import android.content.DialogInterface;
 
 public class WebPlayer extends Activity implements api {
 	public static final int MENU_SHARE = Menu.FIRST;
 	public static final int MENU_GAME = Menu.FIRST + 1;
 	public static final int MENU_VIDEO = Menu.FIRST + 2;
 	private Context myContext;
 	
 	String url;
 	GBObject game = new GBObject();
 	List<String> videos = new ArrayList<String>();
 	private String css = "body{background:#222;color:#FFF;}a{color:#FF0;text-decoration:none;}a:hover{text-decoration:underline;}#three-col .container{border-left:180px solid #222;border-right:330px solid #222;border-color:transparent;}#main .container{border-left:180px solid #050505;border-color:transparent;}#main .right{background:transparent;}#three-col-sky .container{border-left:180px solid #222;border-right:190px solid #222;}#sky-center{width:100%;border-right:175px solid #292929;border-color:transparent;}#mpu-container{padding-right:310px;}#mpu-center{border-right:310px solid #181818;border-color:transparent;margin-right:-310px;}.tabs li a.open{color:#ffb400;background:#000;}.tabs{background:#333;}.corner{display:block;padding-right:10px;overflow:hidden;}.corner:hover{text-decoration:none;}.corner span{background:url(/media/vine/img/black/corners/mgray-left.png) no-repeat scroll top left;display:block;padding:0 0 0 10px;line-height:28px;overflow:hidden;position:relative;font-weight:bold;}.corner span.as-tab{line-height:22px;font-weight:normal;font-size:11px;color:#666;}.content-tabs li a{color:#666;background-color:#333;}.content-tabs li a:hover{color:#fff;}.content-tabs li.on a{background-color:#1a1a1a;}.profile-body{background:#222;}.profile-tabs li a{color:#FFF;background-color:#1A1A1A;}.profile-tabs li a:hover{color:#FF0;}.profile-tabs li.on a{background-color:#000;}.wall-post{background:#111;}.tabs li a{color:#666;}.tabs li a.on{color:#FFF;background:#111;}.side-title{background:#292929;color:#FFF;}#profile .side-title{background:#444;color:#FFF;}.menu-title{background:#292929;color:#FFF;}#menu li a,#gal-menu li a{color:#FFF;background:#111;}#menu li a:hover,#gal-menu li a:hover{color:#FF0;background:#161616;}#menu li a.on,#gal-menu li a.on{background:url(/media/vine/img/generic/menu-arrow.png) no-repeat right #F00;color:#000;}img.bdr-img{padding:4px;background:#111;border:solid 1px #333;margin-top:0;margin-bottom:0;}img.bdr-img-small{padding:3px;background:#111;border:solid 1px #333;border:none;}.top-title-links a:hover{color:#FFF;}.img-gallery-icon{background:#222;border:solid 1px #333;}.t-default .alt{background:none;border-top:solid 1px #222;border-bottom:solid 1px #222;}.btn-plain{margin:0 5px 5px 0;padding:3px 6px 3px 6px;background:#000;border:solid 1px #333;font-size:18px;font-weight:bold;display:inline-block;}h2{border-bottom:solid 1px #333;color:#666;}#wiki h2{margin-top:10px;margin-bottom:10px;}.yellow{color:#FF0;}.orange{color:#ffb400;}.news-link{color:#ffb400;}.t-topics{width:100%;border-collapse:collapse;border:0;}.t-topics th{background:none;color:#666;border-bottom:solid 1px #222;}.t-topics tr:hover{background:#222;}.t-topics .alt{background:none;}.t-topics .t-alt{background:none;}.t-topics td{border-color:#222;}.t-topics .t-link{color:#FFF;}.t-topics .t-link:visited{color:#666;}.t-topics .splitter{background:#111;border:none;border-right:solid 3px #333;}.t-topics .splitter-border{border-top:solid 1px #333;}.t-editor th{color:#FFF;background:#222;}.t-editor.with-border td,.t-editor.with-border th{border:solid 1px #222;}.t-ach{width:100%;border-collapse:collapse;}.t-ach td{border:solid 1px #151515;padding:6px;}.t-ach td.no-border{border:none;}.t-ach tr.alt{background:#222;}.topic-create-border{border-right:solid 1px #333;}table.lists .item-count,table.lists .recommended{color:#999;background-color:#313131;}div.big-number{color:#FFF;background:url(/media/vine/img/black/layout/big-number.png);}.small-number{color:#FFF;border:solid 1px #444;background:#222;}div.content{color:#FFF;}.js-spoiler-btn{background:url(/media/vine/img/icons/exclamation_pad.png) left no-repeat #300;}.js-spoiler-btn a{color:#f66;}.js-spoiler-div{background:#1D0000;}.bdr-alt{background:url(/media/vine/img/icons/exclamation_pad.png) left no-repeat #111;border:none;}.bdr-alt-no-img{background:#111;border:solid 1px #333;}fieldset li{background:#222;}.post .title-Neutral{padding:3px 6px 3px 6px;color:#666;background:#111;}.post .title-hidden{padding:3px 6px 3px 6px;color:#FFF;background:#000;}.post .content a{color:#FF0;}blockquote{border-left:solid 2px #333;color:#AAA;}.form-wrap{background:#111;border:none;}fieldset li{background:#1A1A1A;border:none;}fieldset li.on{background:none;border:none;}fieldset li.buttons{background:#1A1A1A;border-bottom:none;}.text{border:solid 1px #222;background:#111;padding:3px 0 3px 0;}.password{color:#fff;}.text-complete{width:auto;border:solid 1px #333;padding:4px;color:#FFF;background:#111;position:relative;}.text-complete input{color:#FFF;}.text-complete-object{background:#000;border:1px solid #333;color:#FFF;}input[type=\"text\"]{border:solid 1px #333;background:#111;color:#CCC;}input[type=\"password\"]{border:solid 1px #333;background:#111;color:#CCC;}textarea{border:solid 1px #333;background:#111;color:#FFF;}fieldset{background:#1A1A1A;}div.legend{background:#333;border:solid 1px #444;color:#666;}input.selected,textarea.selected{border:solid 1px #666;color:#FFF;}li.selected{background:none;}h1{color:#FFF;}h1 a{color:#FFF;}.editor-gallery{background:#111;border:none;}.editor-rte-loading{background:url(/media/vine/img/generic/loading.gif) no-repeat scroll center;}.editor-paginate{background:#111;border:solid 1px #333;}.editor-popup-titlebar{color:#75b1f5;}.loading{background:url(/media/vine/img/generic/loading.gif) center no-repeat;}.js-item{background:#222;border:solid 1px #333;}.js-item:hover{background:#222;border:solid 1px #FFF;}.js-item-caption{background:#333;color:#FFF;}.js-item-selected{border:solid 1px #FFF;}.js-item-selected .js-item-caption{background:#FFF;color:#000;}.top-title-tabs{position:relative;bottom:-1px;z-index:10;margin:0;padding:0;}.top-title-tabs a{font-size:12px;border:solid 1px #333;background:none;padding:2px 6px 2px 6px;border-bottom:none;color:#FFF;margin-top:2px;}.top-title-tabs a:hover{color:#FFF;padding:3px 6px 3px 6px;background:#292929;margin-top:0;}.top-title-tabs a.on{background:#292929;border:solid 1px #333;padding:3px 6px 3px 6px;border-bottom:none;color:#FFF;font-weight:bold;margin-top:0;}.top-title-tabs.extra-space{margin-top:10px;}.alpha{background:#181818;border:solid 1px #333;padding:5px 10px 5px 10px;width:365px;}.alpha li a{color:#FFF;border:none;}.alpha li a:hover{background:#FFF;color:#000;}.toc{background:#333;border:solid 1px #444;padding:0 10px 10px 10px;width:200px;}.toc a{color:#CCC;}.wiki-img{width:150px;padding:3px;background:#111;color:#FFF;font-weight:bold;text-align:center;border:none;}.wiki-img-small{width:50px;padding:3px;background:#111;color:#FFF;font-weight:bold;text-align:center;border:none;}.wiki-img-medium{width:150px;padding:3px;background:#111;color:#FFF;font-weight:bold;text-align:center;border:none;}.wiki-img-thumb{width:150px;padding:3px;background:#111;color:#FFF;font-weight:bold;text-align:center;border:none;}.wiki-img-screen{width:192px;padding:3px;background:#111;color:#FFF;font-weight:bold;text-align:center;border:none;}.wiki-img-super{padding:3px;color:#FFF;font-weight:bold;text-align:center;border:none;}.wiki-img-left{background:none;float:left;margin:0 15px 15px 0;}.wiki-img-right{background:none;float:right;padding:0 0 15px 15px;}.wiki-img-center{background:none;padding:0 0 15px 0;}#paginate li{line-height:20px;float:left;}#paginate li a{color:#FFF;background:#222;border:none;}#paginate li a:hover{background:#FC3;border:none;color:#000;}#paginate li a.on{background:#000;color:#FFF;border:none;float:left;}.comment-wrap{background:#181818;border:solid 1px #333;padding:10px;}div.comment{color:#FFF;}a.hover-alt:hover{color:#000;}.home-outer-box{background:#181818;border:solid 1px #333;}.home-inner-box{background:#292929;border:solid 1px #333;}.blog-title{border-bottom:solid 3px #333;color:#FFF;background:#111;line-height:26px;}.blog-title a{color:#FFF;text-decoration:none;}.blog-sub-title{border-bottom:dotted 1px #333;}.blog-sub-title a{font-weight:bold;}.blog-option{color:#FFF;}.mini-links{background:#292929;border-bottom:solid 1px #333;color:#666;}.mini-links a{color:#FFF;}.bdr{border:solid 1px #333;}a.issue-link{color:#FFF;}.issue-link.on{border-bottom:solid 1px #111;background:#111;}.issue-link{border:solid 1px #333;border-bottom:none;color:#FFF;}h2.alt{border-bottom:solid 2px #333;color:#FFD200;}.sub-heading{border-bottom:solid 1px #333;}.left-list li a{background:url(/media/vine/img/black/layout/menu-line.gif) bottom no-repeat;}.left-list li a:hover{width:134px;background:#333;}.i2i-alt{background:none;border:none;}.i2i-delete{padding:3px;background:#300;border-top:solid 1px #600;border-bottom:solid 1px #600;}.i2i-add{padding:3px;background:#030;border-top:solid 1px #063;border-bottom:solid 1px #063;}.act-header{border-color:#333;color:#666;}.act-entry{border-color:#222;}.act-body{color:#999;}.act-date{color:#666;}.quote{background:url(/media/vine/img/black/layout/quote-top.gif) top left no-repeat;color:#666;}.quote-in{padding-left:18px;background:url(/media/vine/img/black/layout/quote-bot.gif) bottom right no-repeat;}.staff-flag{background:#000;color:#C00;border:none;}.blog-title{border:none;}.sub-blog-title{padding:3px 6px 3px 6px;color:#CCC;background:#222;border:none;margin-bottom:10px;}.deck{border:solid 1px #333;background:#181818;padding:10px;margin-bottom:10px;}.wiki-header{background:#111;padding:10px;}.img-wrap,{background:#292929;border:solid 1px #333;padding:10px 10px 10px 10px;margin:0 0 10px 0;}.home-h1{background-color:#111;}.home-promos{background-color:#1A1A1A;}.home-promos li{background-color:#000;}.home-promos .wrap{background-color:#000;}.home-promos .deck{color:#fff;}.home-promos-wrap .scroll{background-image:url(/media/vine/img/black/layout/promo-scroll-arrows.png);}.home-releases{background-color:#111;}.home-releases li{background-color:#000;}.home-columns{background-color:#1A1A1A;}.bombcast-bg{background:url(/media/vine/img/black/layout/bombcast-bg.gif) top left #631107 no-repeat;width:300px;height:75px;padding:5px;}.loading-big{height:300px;}.review{background:#222;padding:10px;border:solid 1px #333;width:100%;margin-bottom:10px;}.review-title{font-weight:bold;padding:10px;color:#CCC;background:#181818;border:solid 1px #222;}.review-link{color:#900;}ul.errorlist li{padding:2px 6px 2px 6px;background:#300;color:#FFF;}#footer{background:#000;}.tool-tip{background:url(/media/vine/img/black/layout/bubble.png) top no-repeat;padding:15px 8px 5px 8px;border-bottom:solid 1px #333;}.t-wikid{border:solid 1px #111;}.t-wikid .mod-section{color:#FFF;background:#930;}.t-wikid .mod-section-add{color:#FFF;background:#030;}.t-wikid .mod-delete{color:#FFF;background:#300;}.t-wikid td{border-top:solid 1px #111;border-bottom:solid 1px #111;}.t-wikid .right-bar{border-right:solid 5px #111;}.t-wikid .field-name{background:#111;}.t-mod td a{color:#FFF;}.list-item{border-bottom:none;padding:10px 0 10px 0;}.video-player{background:#111;border:solid 1px #333;}.color-normal{color:#75b1f5;}.color-bright{color:#04C3C3;}.color-invert{color:#FFF;}.pm-unread{color:#FF0;}.js-item-cage .t-topics th{border:solid 1px #333;}.js-item-cage .t-topics td{border:solid 1px #333;}.pop-news{padding:10px;border-bottom:solid 1px #333;}#uploader .progressContainer{border-color:#222;background-color:#111;}#uploader .red{background:#300;border-color:#600;}#uploader .green{background:#030;border-color:#063;}#uploader .blue{border-color:#222;background-color:#111;}#js-friend-feed-sub-ul li a{color:#fff;}#js-friend-feed-sub-ul a.sub-on{color:#ffb400;text-decoration:underline;}img.sel{border:1px solid #ffb400;}.green{color:#51C304;}.opacity-mask{background:#000;opacity:.8;filter:alpha(opacity=80);-moz-opacity:.80;}.btn-icon:hover{color:#FF0;}.position-gallery{border-color:#393939;background-color:#151515;}.position-gallery ul li{background-color:#303030;border-color:#585858;}.position-gallery ul li.on,.position-gallery ul li:hover{background-color:#000;}.gallery-header{background-color:#F00;color:#FFF;}.img-gallery-cage{background:#222;border-color:#222;}.img-gallery-cage.success{background-color:#060;}.img-gallery-edit{background-color:#111;color:#666;}.img-gallery-thumb{background:#000;}a.img-gallery-toggle-small:hover{background-color:#000;}a.img-gallery-toggle-medium:hover{background-color:#000;}a.img-gallery-toggle-large:hover{background-color:#000;}.default-span{color:#FF0;}.gallery-side-title{color:#FFF;}#gallery-menu li a{color:#FFF;}#gallery-menu li a:hover{background:#222;}#gallery-menu li a.on{color:#000;}.profile-body{background:#222;}.editable{border:none;background:#111;}a.none{color:#fff;text-decoration:none;}a.none:hover{color:#fff;text-decoration:underline;}.btn-arrow-down{background:url(/media/vine/img/black/btn/btn-arrow-down.png) top no-repeat;}.btn-arrow-up{background:url(/media/vine/img/black/btn/btn-arrow-up.png) top no-repeat;}.btn-list{background:url(/media/vine/img/black/btn/btn-fav.gif) top left no-repeat;}.btn-list.pull{background:url(/media/vine/img/black/btn/btn-pull.gif) top left no-repeat;}.btn-list.wish{background:url(/media/vine/img/black/btn/btn-wish.gif) top left no-repeat;}.btn-list.new{background:url(/media/vine/img/black/btn/btn-new.gif) top left no-repeat;}.btn-list.collection{background:url(/media/vine/img/black/btn/btn-col.gif) top left no-repeat;}ul.videos .wrap{background-color:#111;}.vine-form button{border-color:#A21111;background-image:url(/media/vine/img/black/layout/btn.png);}.comment-bubble{color:#666;background-image:url(/media/vine/img/generic/comment-bubble.png);}.comment-bubble:hover{color:#FFF;}";
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		ExceptionHandler.register(this, "http://harrism.com/GB/server.php"); 
 		myContext = this;
 		Bundle bundle = getIntent().getExtras();
 		WebView web = new WebView(this);
 		setContentView(web);
 		web.getSettings().setJavaScriptEnabled(true);
 		String predata = "<html><head><style type='text/css'>" + css
 				+ "</style></head><body>";
 		String postdata = "</body></html>";
 		String data = predata + bundle.getString("data") + postdata;
 		game.setId(bundle.getString("gameId"));
 		game.setType(ObjectType.GAME);
 
 		// get videos
 		String vids = bundle.getString("videos");
 		if (vids != null && !vids.equals("")) {
 			String[] vidsArray = vids.split(",");
 			for (String v : vidsArray) {
 				videos.add(v);
 			}
 		}
 
 		web.loadDataWithBaseURL(bundle.getString("URL"), data, null, "utf-8",
 				null);
 
 		url = bundle.getString("URL");
 	}
 
 	public Dialog onCreateDialog(int i) {
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
 		builder.setMessage("Shorten URL?")
 		       .setCancelable(false)
 		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	    share(true);
 		                dialog.cancel();
 		           }
 		       })
 		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	    share(false);
 		                dialog.cancel();
 		           }
 		       });
 		AlertDialog alert = builder.create();
 		
 		return alert;
 	}
 	
 	public void share(boolean shorten) {
 		Intent shareIntent = new Intent(
 				android.content.Intent.ACTION_SEND);
 		shareIntent.setType("text/plain");
 		if(!shorten) {
 			shareIntent.putExtra(Intent.EXTRA_TEXT, url);
 		} else {
 			shareIntent.putExtra(Intent.EXTRA_TEXT, Bitly.getShortUrl(url));
 			
 		}
 		startActivity(Intent.createChooser(shareIntent,
 				"Share link with..."));
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuItem share = menu.add("Share");
 		share.setIcon(android.R.drawable.ic_menu_share);
 
 		share.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				showDialog(1);
 				return true;
 			}
 		});
 
 		if (game.getId() != null) {
 			final Activity activity = this;
 
 			MenuItem viewGame = menu.add("View Game");
 			viewGame.setIcon(android.R.drawable.ic_menu_view);
 			viewGame.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
 				@Override
 				public boolean onMenuItemClick(MenuItem item) {
 					Intent myIntent = new Intent(activity, GBObjectViewer.class);
 					Bundle bundle = new Bundle();
 					bundle.putString("id", game.getId());
 					bundle.putString("type", game.getType().toString());
 					myIntent.putExtras(bundle);
 					WebPlayer.this.startActivity(myIntent);
 
 					return true;
 				}
 
 			});
 
 		}
 
 		if (videos.size() > 0) {
 			if (videos.size() == 1) {
 				MenuItem mi = menu.add("Watch Video");
 				mi.setIcon(android.R.drawable.ic_media_play);
 				
 				final String video = videos.get(0);
 
 				mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
 					@Override
 					public boolean onMenuItemClick(MenuItem item) {
 						if (video.contains("youtube.com")) {
 							playYoutubeVideo(video);
 						} else if (video.contains("giantbomb.com")) {
 							playGiantbombVideo(video);
 						}
 						return true;
 					}
 
 				});
 
 			} else {
 				SubMenu vids = menu.addSubMenu("Watch Videos");
 				vids.setIcon(android.R.drawable.ic_media_play);
 				for (String v : videos) {
 					MenuItem mi = vids.add(v);
 					final String video = v;
 					mi
 							.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
 								@Override
 								public boolean onMenuItemClick(MenuItem item) {
 									if (video.contains("youtube.com")) {
 										playYoutubeVideo(video);
 									} else if (video.contains("giantbomb.com")) {
 										playGiantbombVideo(video);
 									}
 									return true;
 								}
 
 							});
 				}
 			}
 		}
 
 		return true;
 	}
 
 	private void playYoutubeVideo(String url) {
 		startActivity(new Intent(Intent.ACTION_VIEW, Uri
 				.parse(url.split("&")[0])));
 	}
 
 	private void playGiantbombVideo(String url) {
 
		Pattern idPattern = Pattern.compile("[0-9]+-([0-9]+)");
 		Matcher idMatcher = idPattern.matcher(url);
 
 		if (idMatcher.find()) {
 			VideoFeedParser parser = new VideoFeedParser(
 					"http://api.giantbomb.com/video/"
							+ idMatcher.group(1)
 							+ "/?api_key="
 							+ API_KEY
 							+ "&sort=-publish_date&limit=25&field_list=name,deck,id,url,image,site_detail_url&format=xml",
 					true);
 
 			List<Video> vids = parser.parse();
 			if (vids.size() > 0) {
 				Video vid = vids.get(0);
 
 				Intent myIntent = new Intent(this, VidPlayer.class);
 				Bundle bundle = new Bundle();
 				bundle.putString("URL", vid.getLink());
 				bundle.putString("title", vid.getTitle());
 				bundle.putString("siteDetailURL", vid.getSiteDetailURL());
 				myIntent.putExtras(bundle);
 				startActivity(myIntent);
 			}
 		}
 	}
 }

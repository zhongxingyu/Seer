 package com.madalla.webapp.google;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.validation.validator.RangeValidator;
 
 import com.madalla.bo.page.PageData;
 import com.madalla.bo.video.VideoPlayerData;
 import com.madalla.webapp.CmsPanel;
 import com.madalla.webapp.CmsSession;
 import com.madalla.webapp.css.Css;
 import com.madalla.wicket.configure.AjaxConfigureIcon;
 
 
 public class YoutubePlayerPanel extends CmsPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Constructor
 	 *
 	 * @param id wicket id
 	 * @param page Page Name
 	 */
 	public YoutubePlayerPanel(String id, String page) {
 		super(id);
 		add(Css.CSS_FORM);
 		add(Css.CSS_BUTTONS);
 		final VideoPlayerData videoData = getVideo(id, page);
 		String url = "http://www.youtube.com/v/"+videoData.getVideoId()+"&enablejsapi=1&playerapiid=ytplayer";
 		add(new SwfObject(url,videoData.getWidth(), videoData.getHeight()));
 
 		WebMarkupContainer playerDiv = new WebMarkupContainer("playerDiv");
 		playerDiv.setOutputMarkupId(true);
 		add(playerDiv);
 
 		Form<VideoPlayerData> form = new Form<VideoPlayerData>("videoForm", new CompoundPropertyModel<VideoPlayerData>(videoData));
 		playerDiv.add(form);
 		form.setOutputMarkupId(true);
 		form.add(new AjaxSubmitLink("submit"){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 				target.appendJavascript("playVideo('"+videoData.getVideoId()+"','"+ videoData.getWidth()+"','"+videoData.getHeight()+"');");
 				saveVideo(videoData);
 				//AjaxConfigureIcon.hide(target, "formDiv");
 			}
 
 		});
 		form.add(new RequiredTextField<String>("videoId"));
 		form.add(new RequiredTextField<Integer>("height").add(new RangeValidator<Integer>(10,700)));
 		form.add(new RequiredTextField<Integer>("width").add(new RangeValidator<Integer>(10,900)));
 
 		if (((CmsSession)getSession()).isLoggedIn()){
 			add(new AjaxConfigureIcon("configureIcon", playerDiv, 14));
 		} else {
 			add(new Label("configureIcon"));
 		}
 
 	}
	
	

	@Override
	protected void onBeforeRender() {
		setOutputMarkupId(true);
		super.onBeforeRender();
	}
 
 	private VideoPlayerData getVideo(String id, String page){
 		PageData pageData = getRepositoryService().getPage(page);
 		return getRepositoryService().getVideoPlayerData(pageData, id);
 	}
 
 	private void saveVideo(VideoPlayerData data){
 		getRepositoryService().saveVideoPlayerData(data);
 	}
 
 }

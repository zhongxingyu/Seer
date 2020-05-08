 package org.fhw.asta.kasse.client.controller;
 
 import org.fhw.asta.kasse.client.event.LoginEvent;
 import org.fhw.asta.kasse.client.event.LoginEventHandler;
 import org.fhw.asta.kasse.client.widget.topbar.TopBarWidget;
 import org.fhw.asta.kasse.client.widget.topbar.ready.ReadyTopBarWidget;
 import org.fhw.asta.kasse.shared.model.Article;
 import org.fhw.asta.kasse.shared.service.article.ArticleServiceAsync;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasText;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class TopbarController {
 
 	@Inject
 	private ReadyTopBarWidget readyTopbarWidget;
 	
 	@Inject
 	private TopBarWidget topbarWidget;
 	
 	@Inject
	private BasketController basketController;
 	
 	@Inject
 	private ArticleServiceAsync articleService;
 	
 	@Inject
 	public void init(EventBus eventBus) {
 		eventBus.addHandler(LoginEvent.TYPE, new LoginHandler());
 	}
 	
 	private void onLogin() {
 		topbarWidget.setWidget(readyTopbarWidget);
 		
 		readyTopbarWidget.getQuickBoxHandlers().addChangeHandler(new QuickBoxHandler());
 	}
 	
 	private class QuickBoxHandler implements ChangeHandler {
 
 		@Override
 		public void onChange(ChangeEvent event) {
 			HasText quickBox = readyTopbarWidget.getQuickBox();
 			String qText = quickBox.getText();
 			
 			if(qText.matches("[a-zA-Z][0-9]+")){
 				if(qText.matches("[aA][0-9]+")){
 					articleService.getArticleById(qText.replaceAll("[a-zA-Z]", ""), new ArticleCallback());
 				} // else if(qText.matches.....
 			}
 			
 			quickBox.setText("");
 		}
 		
 	}
 	
 	private class LoginHandler implements LoginEventHandler {
 
 		@Override
 		public void onLogin(LoginEvent loginEvent) {
 			TopbarController.this.onLogin();
 		}
 		
 	}
 	
 	private class ArticleCallback implements AsyncCallback<Article> {
 
 		@Override
 		public void onFailure(Throwable caught) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onSuccess(Article result) {
 			basketController.addBasketPosition(result);			
 		}
 		
 	}
 	
 	
 }

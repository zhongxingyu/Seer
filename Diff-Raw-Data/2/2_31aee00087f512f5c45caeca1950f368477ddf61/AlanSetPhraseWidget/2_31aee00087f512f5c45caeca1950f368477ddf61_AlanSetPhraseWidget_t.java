 package com.game.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class AlanSetPhraseWidget extends Composite {
 	private final EPYC_game game;
 	private final GreetingServiceAsync greetingService;
 	
 	private Panel pictures;
 
 	public AlanSetPhraseWidget(final EPYC_game game,
 			final GreetingServiceAsync greetingService) {
 		this.game = game;
 		this.greetingService = greetingService;
 
 		drawMe();
 		
 		/*
 		final VerticalPanel vpanel = new VerticalPanel();
 		initWidget(vpanel);
 		final HorizontalPanel hpanel1 = new HorizontalPanel();
 		final HorizontalPanel hpanel2 = new HorizontalPanel();
 		final TextBox enter_phrase_box = new TextBox();
 		final Button enter_phrase_button = new Button("Submit Phrase");
 		hpanel2.add(enter_phrase_box);
 		hpanel2.add(enter_phrase_button);
 		vpanel.add(hpanel1);
 		vpanel.add(hpanel2);
 		*/
 
 
 	}
 	
 	public void drawMe() {
 		ArrayList<String> input = new ArrayList<String>();
 		input.add(game.username);
 		
 		pictures = new FlowPanel();
 		final TextBox inputSentence = new TextBox();
 		
 		KeyUpHandler handler = new KeyUpHandler() {
 			public void onKeyUp(KeyUpEvent event) {
 				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
 					sendSentence();
 				}
 			}
 
 			private void sendSentence() {
 				ArrayList<String> input = new ArrayList<String>();
 				input.add(game.username);
 				input.add(inputSentence.getText());
 				String sentence = inputSentence.getText();
 				greetingService.SubmitPhraseGame(input,
 						new AsyncCallback<ArrayList<String>>() {
 							public void onFailure(Throwable caught) {
 								// Show the RPC error message to the user
 							}
 
 							public void onSuccess(ArrayList<String> result) {
 								/* todo: send sentence to server */
 								game.advance();
 							}
 						});
 			}
 		};
 
 		FlowPanel box = new FlowPanel();
 		FlowPanel inputPanel = new FlowPanel();
 		box.add(pictures);
 		box.add(inputPanel);
 		inputPanel.add(inputSentence);
 		
 		pictures.addStyleName("pictures");
 		
 		inputSentence.addKeyUpHandler(handler);
 		inputSentence.setFocus(true);
 		
 		initWidget(box);
 		fetchPictures(input);
 	}
 	
 	public void fetchPictures(ArrayList<String> input) {
 		greetingService.GetImagesGame(input,
 				new AsyncCallback<ArrayList<String>>() {
 					public void onFailure(Throwable caught) {
 						
 					}
 
 					@Override
 					public void onSuccess(ArrayList<String> result) {
 						// Lets Display all these pictures
 						if (result == null)
 							return;
 						for (int i = 0; i < Math.min(5, result.size()); i++) {
							if(result.get(i).length() == 0)
								continue;
 							Image image = new Image(result.get(i));
 							pictures.add(image);
 						}
 					}
 				});
 	}
 }

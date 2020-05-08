 package lo52.messaging.fragments;
 
 import lo52.messaging.R;
 import lo52.messaging.activities.ConversationPagerActivity;
 import lo52.messaging.model.Conversation;
 import lo52.messaging.model.Message;
 import lo52.messaging.services.NetworkService;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * Contenu d'un "tab" (fragment) à l'intérieur de ConversationPagerActivity.
  */
 public class ConversationFragment extends Fragment {
 
 	private static final String TAG = "ConversationFragment";
 	final ConversationFragment thisFrag = this;
 
 	// La conversation associée à ce fragment
 	private Conversation conversation = null;
 
	int conversation_id;
 	View fv;
 
 	String conversationName_str = "";
 	String conversationText_str = "";
 
 	ConversationPagerActivity parentActivity;
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		// Rafraichit le texte
 		EditText conversText_edit = (EditText) fv.findViewById(R.id.conversation_content);
 
 		// Met à jour la conversation en reprenant celle du service. Permet d'avoir un objet Conversation à jour,
 		// qui peut contenir des messages qui ont été reçus pendant que le fragment était onPause.
 		updateConversationFromService();
 
		if (conversText_edit != null) {
 
 			conversText_edit.setText(Html.fromHtml(conversation.generateUserFriendlyConversationText(parentActivity.getBaseContext())));
 		}
 	}
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		// Inflate le layout depuis le xml
 		fv = inflater.inflate(R.layout.conversation, container, false);
 
 		// Initialise les différents éléments du layout
 		TextView conversName_tv 		= (TextView) fv.findViewById(R.id.conversation_name);
 		EditText conversText_edit		= (EditText) fv.findViewById(R.id.conversation_content);
 		//EditText conversUserText_edit	= (EditText) v.findViewById(R.id.conversation_usermessage);
 		Button conversMedia_btn 	= (Button) fv.findViewById(R.id.conversation_media_button);
 		Button conversSend_btn 		= (Button) fv.findViewById(R.id.conversation_send_button);
 
 		// On rend le Text Edit de la convers non éditable
 		//conversText_edit.setEnabled(false);
 		conversText_edit.setFocusable(false);
 
 		parentActivity = (ConversationPagerActivity) getActivity();
 
 		// Initialise leur valeur
 		conversName_tv.setText(conversationName_str);
 		conversationText_str = conversation.generateUserFriendlyConversationText(parentActivity.getBaseContext());
 		conversText_edit.setText(Html.fromHtml(conversation.generateUserFriendlyConversationText(parentActivity.getBaseContext())));
 
 		conversMedia_btn.setOnClickListener(mediaButtonClickListener);
 		conversSend_btn.setOnClickListener(sendButtonClickListener);
 
 		return fv;
 	}
 
 	// Création et assignation des onClickListerners
 	OnClickListener mediaButtonClickListener = new OnClickListener() {
 		public void onClick(View v) {
 			Log.d(TAG, "Click bouton media");
 
 			parentActivity.startFilePickerActivity();
 		}
 	};
 
 	OnClickListener sendButtonClickListener = new OnClickListener() {
 		public void onClick(View v) {
 			String messageText = getConversUserText();
 
 			if (messageText.equals("")) return;
 			
 			
 			// Ajout du message texte à la conversation locale
 			conversation.addMessage(new Message(NetworkService.getUser_me().getId(), messageText));
 			
 			/*Log.d(TAG, "Update convers");
 			updateConversationFromService();
 			Log.d(TAG, "Updated convers");*/
 			
 			// Mise à jour de l'UI
 			tryTextRefresh();
 
 			// Reset du champ d'entrée de texte			
 			EditText conversUserText_edit	= (EditText) fv.findViewById(R.id.conversation_usermessage);
 			conversUserText_edit.setText("");
 			
 			parentActivity.onFragmentSendButtonClick(messageText, getConversation_id());
 		}
 	};
 
 	/**
 	 * Met à jour son objet Conversation en prenant celui du service, qui est plus à jour
 	 */
 	public void updateConversationFromService() {
 		if (conversation != null)	// car peut ne pas avoir encore été initialisé par le pager
 			conversation = NetworkService.getListConversations().get(conversation.getConversation_id()); 
 	}
 
 
 	/**
 	 * 
 	 * Getters / Setters
 	 * 
 	 */
 
 	public void setConversation(Conversation c) {
 		conversation = c;
 	}
 
 	/**
 	 * Retourne l'ID associé à cette conversation
 	 * @return
 	 */
 	public int getConversation_id() {
 		return conversation_id;
 	}
 
 	/**
 	 * Attribue un ID à la conversation. Doit (devrait) être appelé une fois que le fragment de la conversation a été créé.
 	 * @param conversation_id
 	 */
 	public void setConversation_id(int conversation_id) {
 		this.conversation_id = conversation_id;
 	}
 
 	/**
 	 * Met à jour le nom de la conversation, affiché en haut du fragment de conversation 
 	 * @param conversName
 	 */
 	public void setConversName(String conversName) {
 		// FIXME éventuellement, histoire de faire plus classe. Mais là ca crashe
 		//conversationName_str = getString(R.string.conversations_name_prefix);
 		conversationName_str = conversName;
 	}
 
 	/**
 	 * Retourne le texte contenu dans le champ de conversation
 	 * @return
 	 */
 	public String getConversText() {
 		return conversationText_str;
 	}
 
 	/**
 	 * Met du texte dans le champ de conversation de la vue. Attention, si du texte est déjà présent il sera écrasé.
 	 * @deprecated utiliser la méthode pour générer le texte depuis la conversation
 	 * @param conversText
 	 * @see appendConversationText()
 	 */
 	public void setConversText(String conversText) {
 		this.conversationText_str = conversText;
 	}
 
 
 	/**
 	 * Retourne le texte entré par l'utilisateur
 	 * @return
 	 */
 	public String getConversUserText() {
 		EditText conversUserText_edit = (EditText) fv.findViewById(R.id.conversation_usermessage);
 		return conversUserText_edit.getText().toString();
 	}
 
 
 	/**
 	 * Set le texte dans le champ dans lequel l'uilisateur écrit. Ne devrait pas être utilisé directement.
 	 * @param conversUserText
 	 */
 	/*public void setConversUserText(String conversUserText) {
 		this.conversUserText_edit.setText(conversUserText);
 	}*/
 
 
 	/**
 	 * Ajoute du texte à la suite du texte présent dans le champ de conversation sans écraser le texte présent
 	 * @deprecated Ajouter le message reçu à la conversation du fragment et regénérer le corps du texte de la conversation
 	 * @param text
 	 */
 	public void appendConversationText(String text) {
 		//this.setConversText(this.getConversText() + " " + text);
 		this.conversationText_str += " " + text;
 	}
 
 
 	/**
 	 * @deprecated
 	 * @return
 	 */
 	public View getFragmentView() {
 		return fv;
 	}
 
 	/**
 	 * @deprecated
 	 * @return
 	 */
 	public ConversationFragment getThisFrag() {
 		return thisFrag;
 	}
 
 	public Conversation getConversation() {
 		return conversation;
 	}
 
 	/**
 	 * Essaye de regénérer le texte de la conversation si la vue est active
 	 */
 	public void tryTextRefresh() {
 		// Régénère le texte de la conversation
 		conversationText_str = conversation.generateUserFriendlyConversationText(parentActivity.getBaseContext());
 		Log.d(TAG, "Texte mis à jour " + conversationText_str);
 		
 		// Essaye de rafraichir le textEdit
 		if (fv != null) {
 			EditText conversText_edit = (EditText) fv.findViewById(R.id.conversation_content);
 			if (conversText_edit != null)
 				conversText_edit.setText(Html.fromHtml(conversationText_str));
 			else Log.d(TAG, "Champ de texte null");
 		} else {
 			Log.d(TAG, "N'a pas pu refresh la vue");
 		}
 
 	}
 
 }

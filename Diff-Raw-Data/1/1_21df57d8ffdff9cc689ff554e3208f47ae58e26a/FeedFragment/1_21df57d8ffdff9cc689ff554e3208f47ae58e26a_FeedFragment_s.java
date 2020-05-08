 package fragments;
 
 import com.alexismorin.linguage.se.sv.R;
 import com.alexismorin.linguage.se.sv.R.dimen;
 import com.alexismorin.linguage.se.sv.R.drawable;
 import com.alexismorin.linguage.se.sv.R.id;
 import com.alexismorin.linguage.se.sv.R.layout;
 import com.alexismorin.linguage.util.ChallengeCard;
import com.alexismorin.linguage.util.MyPlayCard;
 import com.fima.cardsui.objects.CardStack;
 import com.fima.cardsui.views.CardUI;
 import com.slidingmenu.lib.SlidingMenu;
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView.FindListener;
 import android.widget.TextView;
 
 public class FeedFragment extends Fragment {
 
 	protected ListFragment mFrag;
 	protected CardUI mCardView;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.activity_feed, container, false);
 		//TextView tv = (TextView) view.findViewById(R.id.todaysChallengesLabel);
 		
 		
 		mCardView = (CardUI) view.findViewById(R.id.challengeCards);
 		mCardView.setSwipeable(false);
 		
 		mCardView.addCard(new ChallengeCard("Greetings in Swedish", "(Hälsningar)", R.drawable.challenge_hand));
 		mCardView.addCard(new ChallengeCard("Build a sentence", "(Bygg en mening)", R.drawable.challenge_order));
 		mCardView.addCard(new ChallengeCard("At the Doctor's", "(Hos läkaren)", R.drawable.challenge_conversation));
 		mCardView.addCard(new ChallengeCard("Tag a photograph", "(Tagga en bild)", R.drawable.challenge_photo));
 		mCardView.addCard(new ChallengeCard("Learn with friends", "(Lär dig med kompisar)", R.drawable.challenge_facebook));
 		
 		mCardView.refresh();
 		
 		return view;
 	}
 	
 	public void onActivityCreated(Bundle savedInstanceState){
 		super.onActivityCreated(savedInstanceState);	
 	}
 }

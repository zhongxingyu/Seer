 package md.vcroitor.licenta.client.fragment;
 
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import md.vcroitor.licenta.client.R;
 import md.vcroitor.licenta.client.activity.PagerSlidingActivity;
 import md.vcroitor.licenta.client.adapter.ShopListAdapter;
 import md.vcroitor.licenta.client.domain.Shop;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: Vitalie Croitor
  * Date: 5/3/13
  * Time: 11:48 AM
  */
 public class ShopListFragment extends ListFragment {
 
     private ShopListAdapter adapter;
     private PagerSlidingActivity parentActivity;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         List<Shop> shops = new ArrayList<Shop>();
 
         Shop nefertit = new Shop();
         nefertit.setImage("http://s.zumzi.com/md/place/9/6/9629e98a88819ee5b3ad72f4b81ecefd_1_square.jpg");
         nefertit.setName("Lounge Cafe Nefertiti");
         nefertit.setRating(4);
         nefertit.setCategory("Others");
         nefertit.setNrOfPromotions("25");
 
         Shop game = new Shop();
         game.setImage("http://s.zumzi.com/md/place/a/0/a0bb35776dcb1924b984d3b4c03850b8_1_square.jpg");
         game.setName("Game World");
         game.setRating(3);
         game.setCategory("Toys & Games");
         game.setNrOfPromotions("10");
 
         Shop elpaso = new Shop();
         elpaso.setImage("http://s.zumzi.com/md/place/b/6/b6f43027a4b86bb1538fc6d06dfc7683_1_square.jpg");
         elpaso.setName("El Paso");
         elpaso.setRating(5);
         elpaso.setCategory("Food & Drinks");
         elpaso.setNrOfPromotions("35");
 
         Shop autocinema = new Shop();
         autocinema.setImage("http://s.zumzi.com/md/provider/square/c4e69b3070c8ae3a3617e0037667923b.jpg");
         autocinema.setName("Autocinema");
         autocinema.setRating(5);
         autocinema.setCategory("Fun");
         autocinema.setNrOfPromotions("5");
 
         shops.add(elpaso);
         shops.add(autocinema);
         shops.add(nefertit);
         shops.add(game);
 
         parentActivity = (PagerSlidingActivity) getActivity();
         adapter = new ShopListAdapter(getActivity(), shops);
         setListAdapter(adapter);
         parentActivity.getSearchField().addTextChangedListener(textWatcher);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         // Inflate the layout containing a title and body text.
         return inflater.inflate(R.layout.shop_list_fragment, container, false);
     }
 
     private TextWatcher textWatcher = new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         }
 
         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {
         }
 
         @Override
         public void afterTextChanged(Editable s) {
             doSearchShops(s.toString().trim());
         }
     };
 
     private void doSearchShops(String text) {
         adapter.getFilter().filter(text);
     }
 }

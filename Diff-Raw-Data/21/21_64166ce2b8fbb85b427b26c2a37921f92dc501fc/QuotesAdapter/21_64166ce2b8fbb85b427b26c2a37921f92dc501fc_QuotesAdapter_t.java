 package pt.feup.stockportfolio;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.animation.Animator;
 import android.animation.AnimatorSet;
 import android.animation.AnimatorSet.Builder;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class QuotesAdapter extends ArrayAdapter<Quote> 
 {
 	class QuoteAsync extends AsyncTask<Object, Void, Void>
 	{
 		Quote quote = null;
 		View view = null;
 
 		@Override
 		protected Void doInBackground(Object... arg0) {
 			quote = (Quote) arg0[0];
 			view = (View) arg0[1];
 			if (!quote.isUpdated)
 				try {
 					quote.update();
 				} catch (NoInternetException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			return null;
 		}
 		@Override
 		protected void onPostExecute(Void result) {
 			view.findViewById(R.id.updating).setVisibility(View.GONE);
 			if (quote.isUpdated)
 			{
 				((ImageView) view.findViewById(R.id.button)).setImageResource(R.drawable.plus);
 				((TextView) view.findViewById(R.id.close)).setText("" + quote.getLast().close);
 			}
 		}
 
 	}
 	
 	
 	public QuotesAdapter(Context _context, int resource,
 			ArrayList<Quote> _objects) {
 		super(_context, resource, _objects);
 		context = _context;
 		objects = _objects;
 
 	}
 
 	Context context;
 	ArrayList<Quote> objects;
 	ArrayList<View> swipable = new ArrayList<View>();
 	private int removedItems = 0;
 
 	public void hideSwipables()
 	{
 		removedItems = swipable.size();
 		Animation fadeOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_left);
 		fadeOutAnimation.setRepeatCount(0);
 		fadeOutAnimation.setFillAfter(true);
 
 		for (View v : swipable)
 		{
 			v.startAnimation(fadeOutAnimation);
 		}
 		fadeOutAnimation.setAnimationListener(new AnimationListener(){
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				int i = 0;
 				for (i = 0; i < objects.size(); )
 				{
 					if (objects.get(i) instanceof QuoteUpdate)
 					{
 						remove(objects.get(i));
 						continue;
 					}
 					++i;
 				}
 				
 				notifyDataSetChanged();
 
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 				// TODO Auto-generated method stub
 
 			}
 
 		});
 
 
 
 	}
 
 
 
 	@Override
 	public View getView(final int position, View convertView, final ViewGroup parent)
 	{
 		
 		final Quote quote = objects.get(position);
 		if (quote == null)
 			return new View(context);
 		View view = convertView;
 		View recycled = null;
 		if (view != null)
 		{
 			try
 			{
 			if ((((TextView) view.findViewById(R.id.quote)).getText() != quote.tick) && (!(quote instanceof QuoteUpdate)))
 			{
 				recycled = view;
 				view = null;
 				//Log.e("HELLO QUOTES", quote.tick + " recycled forcefully.");
 			}
 			}
 			catch (NullPointerException e)
 			{
 				view = null;
 			}
 		}
 		if (view == null) 
 		{
 			//Log.e("HELLO QUOTES", "Refreshing a view!");
 			final LayoutInflater inflator = ((Activity) getContext()).getLayoutInflater();
 
 
 			if(quote instanceof QuoteUpdate)
 			{
 				if (recycled != null)
 					view = recycled;
 				else
 				{
 					if (position == 0)
 						view = inflator.inflate(R.layout.quote_update_first, null);
 					else
 						view = inflator.inflate(R.layout.quote_update, null);
 				}
 				((TextView) view.findViewById(R.id.quote)).setText(quote.tick);
 				((TextView) view.findViewById(R.id.worth)).setText(Utils.sanitize(("$" + (((QuoteUpdate) quote).change))));
 				((TextView) view.findViewById(R.id.shares)).setText("on your " + quote.quantity + " shares");
 				
 				((RelativeLayout) view.findViewById(R.id.update_background)).setBackgroundColor(quote.color);
 				view.setOnClickListener(new OnClickListener(){
 
 					@Override
 					public void onClick(View v) {
 						
 						hideSwipables();
 					}
 
 
 				});
 				if (!swipable.contains(view))
 					swipable.add(view);
 
 				
 			}
 			else
 			{
 				if (quote instanceof QuoteSeparator)
 				{
 					view = inflator.inflate(R.layout.my_portfolio, null);
 					view.setOnClickListener(new OnClickListener(){
 
 						@Override
 						public void onClick(View v) {
 							QuotesActivity act = (QuotesActivity) context;
 							final boolean landscape = (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
 							
 							if (act.inspectingQuotes)
 								act.showExtraFragment(act.p_frag);
 							
 							act.inspectingQuotes = false;
 							if (!landscape)	
 								new Handler().postDelayed(new Runnable() {
 
 
 									@Override
 									public void run() {
 										((QuotesActivity) context).mDrawerLayout.closeDrawer(Gravity.LEFT);
 									}
 								}, 500);
 						}
 
 					});
 				} 
 				else
 				{
 					if (quote instanceof QuoteAdd)
 					{
 						view = inflator.inflate(R.layout.add_quote, null);
 						view.setOnClickListener(new OnClickListener(){
 
 							
 
 							@Override
 							public void onClick(View v) {
 
 								final AlertDialog alertDialog;
 								AlertDialog.Builder builder = new AlertDialog.Builder(context);
 								final View myView = inflator.inflate(R.layout.fragment_add_quote_2, null);
 								final int[] share_number = {0};
 								final EditText ticker = (EditText) myView.findViewById(R.id.ticker);
 								final TextView shares = (TextView) myView.findViewById(R.id.shares);
 
 
 								builder.setView(myView);
 
 
 								alertDialog = builder.create();
 
 								myView.findViewById(R.id.increaseshares).setOnClickListener(new OnClickListener(){
 
 									@Override
 									public void onClick(View v) {
 
 										share_number[0]++;
 										shares.setText("" + share_number[0]);
 									}
 
 								});
 
 	
 								myView.findViewById(R.id.decreaseshares).setOnClickListener(new OnClickListener(){
 
 									@Override
 									public void onClick(View v) {
 										if (share_number[0] == 0)
 											return;
 										share_number[0]--;
 										shares.setText("" + share_number[0]);
 									}
 
 								});
 
 
 								myView.findViewById(R.id.accept).setOnClickListener(new OnClickListener(){
 
 
 									Quote q = new Quote("NULL", 0);
 									@Override
 									public void onClick(View v) {
 										if (!Utils.hasInternet)
 										{
 											Toast.makeText(getContext(), "Not available offline", Toast.LENGTH_LONG).show();
 											return;
 										}
 										
 										if (q.isUpdated)
 										{
 											q.quantity = share_number[0];
 											Utils.myQuotes.add(q);
 
 											Quote a = objects.get(objects.size()-1);
 											objects.remove(objects.size()-1);
 											objects.add(q);
 											objects.add(a);
 											notifyDataSetChanged();
 											alertDialog.dismiss();
 										}
 										else
 										{
 											myView.findViewById(R.id.updating).setVisibility(View.VISIBLE);
 											q = new Quote(ticker.getText().toString().toUpperCase(), share_number[0]);
 
 											Object[] quot = { q , myView };
 											new QuoteAsync().execute(quot);
 										}
 									}
 
 								});
 
 								alertDialog.show();
 							}
 
 						});
 					}
 					else
 					{
 						final QuotesAdapter hello = this;
 						class GraphAsync extends AsyncTask<Object, Void, Void>
 						{
 							View myView = null;
 							Quote quote = null;
 							@Override
 							protected Void doInBackground(Object... arg0) {
 
 								myView = (View) arg0[0];
 								quote = (Quote) arg0[1];
 								if (!quote.isUpdated)
 									try {
 										quote.update();
 									} catch (NoInternetException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									}
 								return null;
 							}
 							@Override
 							protected void onPostExecute(Void result) {
 								((RelativeLayout) myView.findViewById(R.id.real_background)).setBackgroundColor(quote.color);
 								((TextView) myView.findViewById(R.id.value)).setText(Utils.sanitize("" + quote.value));
 								if (quote.getPercentage() < 0)
 								{
 									((RelativeLayout) myView.findViewById(R.id.tick_box)).setBackgroundResource(R.drawable.border_red);
 									((TextView) myView.findViewById(R.id.value)).setTextColor(Color.parseColor("#ed1c24"));
 								}
 								GraphViewSmall mine = new GraphViewSmall(context, null, quote);
 								int size = ((LinearLayout)myView.findViewById(R.id.graph)).getChildCount();
								boolean found = false;
 								for (int i = 0; i < size; ++i)
 								{
 									if (((LinearLayout)myView.findViewById(R.id.graph)).getChildAt(i) instanceof GraphViewSmall)
 									{
 										((LinearLayout)myView.findViewById(R.id.graph)).removeViewAt(i);
										found = true;
 										break;
 									}
 								}
 								
 								((LinearLayout)myView.findViewById(R.id.graph)).addView(mine);
 								Animation fadeInAnimation = AnimationUtils.loadAnimation(hello.getContext(), R.anim.fade_in);
 								fadeInAnimation.setRepeatCount(0);
 								fadeInAnimation.setFillAfter(true);
								if (!found)
									mine.startAnimation(fadeInAnimation);
								
 								((LinearLayout)myView.findViewById(R.id.graph)).findViewById(R.id.progressBar).setVisibility(View.GONE);
 
 								hello.notifyDataSetChanged();
 							}
 
 						}
 						if (recycled != null)
 							view = recycled;
 						else
 							view = inflator.inflate(R.layout.quote_real, null);
 
 						new GraphAsync().execute(view, quote);
 						((TextView) view.findViewById(R.id.quote)).setText(quote.tick);
 
 
 						final boolean landscape = (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
 
 						view.setOnLongClickListener(new OnLongClickListener(){
 
 							@Override
 							public boolean onLongClick(View arg0) {
 								Animation fadeOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_left);
 								fadeOutAnimation.setRepeatCount(0);
 								fadeOutAnimation.setFillAfter(false);
 								fadeOutAnimation.setAnimationListener(new AnimationListener(){
 
 									@Override
 									public void onAnimationEnd(Animation arg0) {
 										objects.remove(quote);
 										Utils.myQuotes.remove(quote);
 										notifyDataSetChanged();
 									}
 
 									@Override
 									public void onAnimationRepeat(Animation arg0) {
 										// TODO Auto-generated method stub
 										
 									}
 
 									@Override
 									public void onAnimationStart(Animation arg0) {
 										// TODO Auto-generated method stub
 										
 									}});
 								
 								arg0.startAnimation(fadeOutAnimation);
 								
 								
 								return true;
 							}
 						});
 						
 						view.setOnClickListener(new OnClickListener(){
 
 							@Override
 							public void onClick(final View v) {
 								if (quote.isUpdated)
 								{
 									Animation fadeInAnimation = AnimationUtils.loadAnimation(hello.getContext(), R.anim.slide_right);
 									fadeInAnimation.setRepeatCount(0);
 									fadeInAnimation.setStartOffset(((QuotesActivity) context).inspectingQuotes ? 0 : 200);
 									final Animation fadeInAnimationCont = AnimationUtils.loadAnimation(hello.getContext(), R.anim.slide_right_recover);
 									fadeInAnimationCont.setRepeatCount(0);
 									
 									fadeInAnimation.setAnimationListener(new AnimationListener(){
 
 										@Override
 										public void onAnimationEnd(Animation arg0) 
 										{
 											v.startAnimation(fadeInAnimationCont);
 										}
 
 										@Override
 										public void onAnimationRepeat(
 												Animation animation) {
 											
 										}
 
 										@Override
 										public void onAnimationStart(
 												Animation animation) {
 											
 										}
 										
 									});
 									if (!landscape)	
 										new Handler().postDelayed(new Runnable() {
 
 
 											@Override
 											public void run() {
 												((QuotesActivity) context).mDrawerLayout.closeDrawer(Gravity.LEFT);
 											}
 										}, 250 + (((QuotesActivity) context).inspectingQuotes ? 0 : 700));
 									
 									((QuoteDetailsFragment) ((QuotesActivity) context).d_frag).setDetails(quote);
 									
 									if (!((QuotesActivity) context).inspectingQuotes)
 									{
 										((QuotesActivity) context).showExtraFragment(((QuotesActivity) context).d_frag);
 										((QuotesActivity) context).inspectingQuotes = true;
 									}
 									
 									v.startAnimation(fadeInAnimation);
 									
 									
 								}
 							}
 
 						});
 					}}
 			}
 		}
 
 
 		return view;
 
 	}
 
 	@Override
 	public int getViewTypeCount()
 	{
 		//return objects.size() + removedItems;
 		return 4;
 	}
 
 	@Override
 	public int getItemViewType(int position)
 	{
 		
 		
 		int ret = 0;
 		
 		Quote quote = objects.get(position);
 		
 		
 		if (quote instanceof QuoteUpdate)
 			
 		{
 			//ret =  position;
 			ret = 0;
 		}
 		
 		else if (quote instanceof QuoteAdd)
 			//ret = -2;
 			ret = 1;
 		
 		else if (quote instanceof QuoteSeparator)
 			//ret = -2;
 			ret = 2;
 		
 		else //ret = position + removedItems ;
 			ret = 3;
 		//Log.e("HELLO ITEMS", "Get item view type for " + position + ", quote " + quote.tick + " with " + ret);
 		return ret;
 	}
 
 
 
 
 }

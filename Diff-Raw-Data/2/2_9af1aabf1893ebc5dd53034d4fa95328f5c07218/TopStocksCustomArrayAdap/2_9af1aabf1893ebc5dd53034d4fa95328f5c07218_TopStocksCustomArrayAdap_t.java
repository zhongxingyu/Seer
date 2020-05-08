 /*Name:TopStocksCustomArra
  * 
  * Purpose:  Custom ArrayAdapter for the ListView in TopStocks
  * Contains: Each site Icon, stock name, and ticker Symbol
  */
 
 package com.example.stockmarketapp.customadapter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.example.stockmarketapp.PortfolioGetMetrics;
 import com.example.stockmarketapp.R;
 import com.example.stockmarketapp.R.id;
 import com.example.stockmarketapp.R.layout;
 import com.example.stockmarketapp.topstocks.TopListModel;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class TopStocksCustomArrayAdap extends ArrayAdapter<TopListModel> {
 
 	private final List<TopListModel> list;
 	private final Activity context;
 	ArrayList<PortfolioGetMetrics> myStock=new ArrayList<PortfolioGetMetrics>();
 
 	public TopStocksCustomArrayAdap(Activity context, List<TopListModel> list) {
 		super(context, R.layout.top_list_layout, list);
 		this.context = context;
 		this.list = list;
 	}
 
 	//When Loading new Listview, this is what populates each item.  
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = null;
 
 		LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		view = inflator.inflate(R.layout.top_list_layout, parent, false);
 		TextView number = (TextView) view.findViewById(R.id.numbCounter);
 		TextView symbol = (TextView) view.findViewById(R.id.stockSymbol);
 		ImageView [] image = new ImageView[6];    //Get Each Stock Site Image to Show which site recommended each stock
 		image[0]=(ImageView) view.findViewById(R.id.siteImageOne);  //Hide Each Symbol, and then check to see which ones need to be shown
 		image[0].setVisibility(View.INVISIBLE);
 		image[1] = (ImageView) view.findViewById(R.id.siteImageTwo);
 		image[1].setVisibility(View.INVISIBLE);
 		image[2]= (ImageView) view.findViewById(R.id.siteImageThree);
 		image[2].setVisibility(View.INVISIBLE);
 		image[3] = (ImageView) view.findViewById(R.id.siteImageFour);
 		image[3].setVisibility(View.INVISIBLE);
 		image[4]= (ImageView) view.findViewById(R.id.siteImageFive);
 		image[4].setVisibility(View.INVISIBLE);
 		image[5]= (ImageView) view.findViewById(R.id.siteImageSix);
 		image[5].setVisibility(View.INVISIBLE);
 
 		ArrayList al=list.get(position).getSites(); //Gets ArrayList of each Sites in Each list item.
 		for(int i=0; i<al.size();i++){				//If a recommendation contains one of these sites, show its symbol
			if(al.get(i).equals("dividta"))
 				image[0].setVisibility(View.VISIBLE);
 			if(al.get(i).equals("stockTwits"))
 				image[1].setVisibility(View.VISIBLE);
 			if(al.get(i).equals("fool"))
 				image[2].setVisibility(View.VISIBLE);
 			if(al.get(i).equals("investor"))
 				image[3].setVisibility(View.VISIBLE);
 			if(al.get(i).equals("msn"))
 				image[4].setVisibility(View.VISIBLE);
 			if(al.get(i).equals("barchart"))
 				image[5].setVisibility(view.VISIBLE);
 		}
 				
 		TextView name = (TextView) view.findViewById(R.id.stockName);
 		name.setText(list.get(position).getStockName()); //Sets Stock Name for each item
 		number.setText(Integer.toString(position + 1));  //Set Rank number of each stock from most frequent recommendations
 		symbol.setText(list.get(position).getName());    //Sets Stock symbol for each item
 
 		return view;
 	}
 }

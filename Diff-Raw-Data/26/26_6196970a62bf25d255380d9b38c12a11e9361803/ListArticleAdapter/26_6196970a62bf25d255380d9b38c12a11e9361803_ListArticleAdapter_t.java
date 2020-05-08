 package com.taiwan.imageload;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.ptt.food.blog.ArticleActivity;
 import com.ptt.food.blog.R;
 import com.ptt.food.blog.SearchActivity;
 import com.ptt.food.blog.api.DBAPI;
 import com.ptt.food.blog.entity.Article;
 import com.ptt.food.fragment.NewsFragment;
 
 public class ListArticleAdapter extends BaseAdapter {
     
     private Activity activity;
     private ArrayList<Article> data;
     private ArrayList<Article> dataFavorite;
     private static LayoutInflater inflater=null;
     private boolean oneArticle = true;
   
//    private ArrayList<Article> fakeData = new ArrayList<Article>();
     
     public ListArticleAdapter(Activity a, ArrayList<Article> d, boolean oneArticle) {
     	
//    	fakeData.add(new Article(0, "author", "[請益]好吃部落格", "2013.2.11", "", "", 0));
//    	fakeData.add(new Article(0, "author", "[食記]好吃部落格", "2013.2.11", "", "", 0));
//    	fakeData.add(new Article(0, "author", "[電視]好吃部落格", "2013.2.11", "", "", 0));
//    	fakeData.add(new Article(0, "author", "[情報]好吃部落格", "2013.2.11", "", "", 0));
//    	fakeData.add(new Article(0, "author", "[討論]好吃部落格", "2013.2.11", "", "", 0));
//    	fakeData.add(new Article(0, "author", "[抱怨]好吃部落格", "2013.2.11", "", "", 0));
//    	fakeData.add(new Article(0, "author", "[廣宣]好吃部落格", "2013.2.11", "", "", 0));
//    	fakeData.add(new Article(0, "author", "[公告]好吃部落格", "2013.2.11", "", "", 0));
//    	fakeData.add(new Article(0, "author", "[吃喝]好吃部落格", "2013/02/11", "", "", 0));
         
     	activity = a;
//    	data = fakeData;
        data=d;
         this.oneArticle = oneArticle;
         inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         dataFavorite = DBAPI.getAllArticles(activity);
         
     }
 
     public int getCount() {
         return data.size();
     }
 
     public Object getItem(int position) {
         return position;
     }
 
     public long getItemId(int position) {
         return position;
     }
     
     public View getView(final int position, View convertView, ViewGroup parent) {
     	
             View vi = inflater.inflate(R.layout.item_list_article, null);
 	        TextView text=(TextView)vi.findViewById(R.id.text_article_title);
 	        TextView textDate=(TextView)vi.findViewById(R.id.text_article_date);
 	        final Button button = (Button)vi.findViewById(R.id.button_item_category);
 	        final CheckBox checkboxFavorite = (CheckBox)vi.findViewById(R.id.checkbox_article);
 	        text.setText(data.get(position).getTitle());
 	        
 	        String titleString = "<font color=#9E1919>"+data.get(position).getTitle()+"</font>"+ "<font color=#AD8440>"+" by "+data.get(position).getAuthor()+"</font>";
 	        text.setText(Html.fromHtml(titleString));
 	        
 	        try{
 //		        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");  
 //		        String dateString = formatter.format(data.get(position).getReleaseTime()); 
 	        	String dateString = data.get(position).getReleaseTime();
 		        textDate.setText(dateString);
 	        }catch(Exception e){
 	        	textDate.setVisibility(View.GONE);
 	        }
 	        
 	        if(data.get(position).getTitle().indexOf("[")!=-1){
 	        	int start = data.get(position).getTitle().indexOf("[")+1;
 	        	int end = data.get(position).getTitle().indexOf("]");
 	        	String btnString = data.get(position).getTitle().substring(start, end);
 	        	button.setText(btnString);
 	        	if(btnString.equals("請益")){
 	        		button.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.custom_button_ask));
 	        	}else if(btnString.equals("電視")){
 	        		button.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.custom_button_tv));
 	        	}else if(btnString.equals("情報")){
 	        		button.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.custom_button_news));
 	        	}else if(btnString.equals("討論")){
 	        		button.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.custom_button_discuss));
 	        	}else if(btnString.equals("抱怨")){
 	        		button.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.custom_button_complain));
 	        	}else if(btnString.equals("廣宣")){
 	        		button.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.custom_button_promote));
 	        	}else if(btnString.equals("食記")){
 	        		button.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.custom_button));        		
 	        	}else{
 	        		button.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.custom_button_others));
 	        	}
 	        	
 	        }else{
 	        	button.setVisibility(View.INVISIBLE);
 	        }
 	        
 	        for(int i =0; i<dataFavorite.size();i++){
 				if(dataFavorite.get(i).getId() == data.get(position).getId()){
 					checkboxFavorite.setChecked(true);
 					break;
 				}else{
 					checkboxFavorite.setChecked(false);
 				}
 			}
 	        
 	        button.setOnClickListener(new OnClickListener() {
 	            @Override
 	            public void onClick(View v) {
 	                // Toast.makeText(activity, "tt", Toast.LENGTH_SHORT).show();
 	            	String keyWord = (String) button.getText();
 	            	Bundle bundle = new Bundle();
                     bundle.putString("SearchKeyword", keyWord);
                     Intent intent = new Intent();
                     intent.setClass(activity, SearchActivity.class);
                     intent.putExtras(bundle);
                     activity.startActivity(intent);
 	            }
 	        });
 	        
 	        
 	        checkboxFavorite.setOnClickListener(new OnClickListener() {
 	            @Override
 	            public void onClick(View v) {
 	                // Toast.makeText(activity, "tt", Toast.LENGTH_SHORT).show();
 	            	if(checkboxFavorite.isChecked()){
 						DBAPI.insertArticle(data.get(position), activity);
 						dataFavorite.add(data.get(position));
 						Toast.makeText(activity, "加入我的最愛", Toast.LENGTH_SHORT).show();
 					}else{
 						DBAPI.deleteArticle(data.get(position), activity);
 						for(int i =0; i<dataFavorite.size();i++){
 							if(dataFavorite.get(i).getId() == data.get(position).getId()){
 								dataFavorite.remove(i);
 							}
 						}
 						Toast.makeText(activity, "從我的最愛移除", Toast.LENGTH_SHORT).show();
 					}
 	            }
 	        });
 	        
 	        vi.setOnClickListener(new OnClickListener() {
 	            @Override
 	            public void onClick(View v) {
 	            	
 	            	int[] idArray = new int[data.size()];
 	            	for (int i=0; i<data.size(); i++){
 	            		idArray[i] = data.get(i).getId();
 	            	}
 	            	
 	                // Toast.makeText(activity, "tt", Toast.LENGTH_SHORT).show();
 	            	Intent intent = new Intent(activity, ArticleActivity.class);
 	                Bundle bundle = new Bundle();
 	                bundle.putIntArray("ArticleIds", idArray);
 	                bundle.putInt("ArticlePosition", position);
 	                if(oneArticle){
 	                	bundle.putInt("PageNum", -1);
 	                }else{
 	                	bundle.putInt("PageNum", NewsFragment.myPage);
 	                }
 	                bundle.putInt("ArticleId", data.get(position).getId());
 	                bundle.putString("ArticleTitle", data.get(position).getTitle());
 	                intent.putExtras(bundle);
 	                activity.startActivity(intent);
 	            }
 
 	        });
 	        
         return vi;
     }
 }

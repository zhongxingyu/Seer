 package com.gmail.va034600.nreader.ui.component;
 
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.gmail.va034600.nreader.R;
 import com.gmail.va034600.nreader.business.constant.NanairoBusinessConstant;
 import com.gmail.va034600.nreader.business.domain.bean.Article;
 
 public class ArticleArrayAdapter extends ArrayAdapter<Article> {
 	private LayoutInflater mInflater;
 
 	public ArticleArrayAdapter(Context context, List<Article> objects) {
 		super(context, 0, objects);
 		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	public View getView(final int position, View convertView, ViewGroup parent) {
 		if (convertView == null) {
 			convertView = mInflater.inflate(R.layout.article_list_row, null);
 		}
 
 		Article article = this.getItem(position);
 		if (article != null) {
 			int color = getColor(article.getMidoku());
 
 			// ID
 			TextView mId = (TextView) convertView.findViewById(R.id.idText);
 			mId.setText(Long.toString(article.getId()));
 			mId.setTextColor(color);
 
 			// タイトル
 			TextView mTitle = (TextView) convertView.findViewById(R.id.nameText);
 			mTitle.setText(article.getTitle());
 			mTitle.setTextColor(color);
 
 			// 公開日時
 			TextView mPublishedDate = (TextView) convertView.findViewById(R.id.publishedDateText);
 			mPublishedDate.setText(article.getPublishedDate());
 			mPublishedDate.setTextColor(color);
 		}
 		return convertView;
 	}
 
 	private int getColor(int midoku) {
 		if (NanairoBusinessConstant.MIDOKU_ON == midoku) {
 			return Color.RED;
 		} else {
			return Color.BLACK;
 		}
 	}
 }

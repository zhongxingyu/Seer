 package com.tw.thoughtblogs;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import com.tw.thoughtblogs.model.Blog;
 
 import java.util.List;
 
 import static android.content.Context.LAYOUT_INFLATER_SERVICE;
 
 public class BlogAdapter extends ArrayAdapter<Blog> {
     private List<Blog> blogs;
 
     public BlogAdapter(Context context, int textViewResourceId, List<Blog> blogs) {
         super(context, textViewResourceId, blogs);
         this.blogs = blogs;
     }
 

     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View v = convertView;
         if (v == null) {
             LayoutInflater vi =
                     (LayoutInflater) this.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.list_item, null);
         }
 
         Blog blog = blogs.get(position);
         if (blog != null) {
             TextView titleView = (TextView) v.findViewById(R.id.title);
             if (titleView != null) {
                 titleView.setText(blog.getTitle());
                 if (blog.getStatus() == 1) {
                     titleView.setTextColor(-1);
                     titleView.setTypeface(Typeface.SERIF, Typeface.BOLD);
                 }
             }
             TextView idView = (TextView) v.findViewById(R.id.blog_id);
            if (idView != null) {
                idView.setText("" + blog.getId());
                idView.setVisibility(View.GONE);
            }
         }
         return v;
     }
 

 }

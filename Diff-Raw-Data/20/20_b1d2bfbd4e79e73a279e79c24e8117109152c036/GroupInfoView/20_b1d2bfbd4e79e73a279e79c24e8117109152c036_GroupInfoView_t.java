 /*******************************************************************
  * Copyright (c) 2010 Rohit Kumbhar
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  *******************************************************************/
 package net.yama.android.views.components;
 
 import net.yama.android.response.Group;
 import net.yama.android.util.Constants;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.text.util.Linkify;
 import android.view.ViewGroup;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TextView;
 
 /**
  * @author Rohit Kumbhar
  *
  */
 public class GroupInfoView extends TableLayout {
 
 	public GroupInfoView(Context context, Group group) {
 		super(context);
 		
 		TextView groupName = new TextView(context);
 		groupName.setText(group.getName());
 		groupName.setTypeface(Typeface.DEFAULT_BOLD);
 		groupName.setPadding(2, 2, 2, 2);
 		groupName.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
 		this.addView(groupName,0);
 		
 		ScrollView scrollView = new ScrollView(context);
 		TextView groupDesc = new TextView(context);
		groupDesc.setText(sanitize(group.getDescription()));
 		scrollView.addView(groupDesc);
 		Linkify.addLinks(groupDesc, Constants.LINK_MASK);
 		this.addView(scrollView, 1);
 		
 	}

	// XXX - Duplicated in EventInfoView
	private CharSequence sanitize(String description) {
		description = description.replaceAll("\r\n", "\n");
		description = description.replaceAll("<br>", "\n");
		description = description.replaceAll("<br />", "\n");
		return description;
	}
 }

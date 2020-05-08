 package com.quanleimu.view;
 
 import java.util.List;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.quanleimu.view.BaseView;
 import com.quanleimu.view.BaseView.ETAB_TYPE;
 import com.quanleimu.view.BaseView.TabDef;
 import com.quanleimu.view.BaseView.TitleDef;
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.CommonItemAdapter;
 import com.quanleimu.entity.FirstStepCate;
 import com.quanleimu.entity.SecondStepCate;
 
 public class SecondCategoryView extends BaseView implements OnItemClickListener {
 	private Bundle bundle;
 	private boolean isPost = false;
 	private FirstStepCate cate = null;
 	private int msgBack = 0xFFFFFFFF;
 
 	public SecondCategoryView(Context content, Bundle bundle, FirstStepCate cate, boolean isPost) {
 		super(content, bundle);
 		this.bundle = bundle;
 		this.isPost = isPost;
 		this.cate = cate;
 		init();
 	}
 	
 	public SecondCategoryView(Context content, Bundle bundle, FirstStepCate cate, boolean isPost, int msgBack) {
 		super(content, bundle);
 		this.bundle = bundle;
 		this.isPost = isPost;
 		this.cate = cate;
 		this.msgBack = msgBack;
 		init();
 	}
 
 	private void init() {
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		View v = inflater.inflate(R.layout.post_othersview, null);
 		this.addView(v);
 
 		CommonItemAdapter adapter = new CommonItemAdapter(this.getContext(), cate.getChildren(), 0x1FFFFFFF, false);
		((ListView)v.findViewById(R.id.post_other_list)).setAdapter(adapter);
		((ListView)v.findViewById(R.id.post_other_list)).setOnItemClickListener(this);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		if(cate == null || cate.getChildren() == null || cate.getChildren().size() <= arg2) return;
 		SecondStepCate secCate = cate.getChildren().get(arg2);
 		if(null != m_viewInfoListener){
 			if(!isPost){
 				bundle.putString("name", secCate.getName());
 				bundle.putString("categoryEnglishName",	secCate.getEnglishName());
 				bundle.putString("siftresult", "");
 				bundle.putString("backPageName", "返回");
 				if(msgBack != 0xFFFFFFFF){
 					String toRet = secCate.englishName + "," + secCate.name;
 					m_viewInfoListener.onBack(msgBack, toRet);
 				}else{
 					m_viewInfoListener.onNewView(new GetGoodsView(getContext(), bundle, secCate.getEnglishName()));
 				}
 			}
 			else{
 				String names = secCate.englishName + "," + secCate.name;
 				if(msgBack != 0xFFFFFFFF){					
 					m_viewInfoListener.onBack(msgBack, names);
 				}else{
 					m_viewInfoListener.onNewView(new PostGoodsView((BaseActivity)getContext(), bundle, names));
 				}
 			}
 		}
 	}
 	
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_title = cate.getName();
 		title.m_leftActionHint = "返回";
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = true;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
 		return tab;
 	}
 
 }

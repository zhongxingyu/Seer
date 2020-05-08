 package com.quanleimu.view;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.FirstStepCate;
 import com.quanleimu.entity.SecondStepCate;
 
 public class CateMainView extends BaseView implements CategorySelectionView.ICateSelectionListener{
 
 	// 定义控件
 	protected CategorySelectionView selectionView;
 	String title_str;
 	String back_str;
 
 	protected void Init(){
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		this.addView((RelativeLayout)inflater.inflate(R.layout.catemain, null));
 
 		selectionView = new CategorySelectionView(getContext(), null, null);
 		selectionView.setSelectionListener(this);
 
 		((LinearLayout)findViewById(R.id.linearListView)).addView(selectionView);
 		
 		title_str = "选择浏览类目";
 		back_str = "";
 	}
 	
 	public CateMainView(Context context){
 		super(context);
 		
 		Init();
 	}
 	public CateMainView(Context context, Bundle bundle){
 		super(context);
 		
 		Init();
 	}
 	
 	@Override
 	public boolean onBack(){
 		if(CateMainView.this.selectionView == null || !CateMainView.this.selectionView.OnBack()){
 			return false;
 		}else
 		{
 			if(null != m_viewInfoListener){
 				this.title_str = back_str;
 				back_str = "";
 				
 				m_viewInfoListener.onTitleChanged(getTitleDef());
 			}
 		}
 		return true;
 	}//called when back button/key pressed
 	
 	@Override
 	public boolean onLeftActionPressed(){
 		return onBack();
 	}//called when left button on title bar pressed, return true if handled already, false otherwise
 	
 	public boolean onRightActionPressed(){return false;}//called when right button on title bar pressed, return true if handled already, false otherwise
 	
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_title = title_str;
 		title.m_leftActionHint = back_str;
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = true;
 		return tab;
 	}
 
 	@Override
 	public void OnMainCategorySelected(FirstStepCate selectedMainCate){
 		if(null != m_viewInfoListener){
 			back_str = title_str;
 			title_str = selectedMainCate.getName();			
 			m_viewInfoListener.onTitleChanged(getTitleDef());
 		}
 	}
 	
 	@Override
 	public void OnSubCategorySelected(SecondStepCate selectedSubCate){
 
 		Bundle bundle = new Bundle();
 		bundle.putString("name", selectedSubCate.getName());
 		bundle.putString("categoryEnglishName",	selectedSubCate.getEnglishName());
 		bundle.putString("siftresult", "");
 		bundle.putString("backPageName", "返回");
 		if(null != m_viewInfoListener){
 			m_viewInfoListener.onNewView(new GetGoodsView(getContext(), bundle, selectedSubCate.getEnglishName()));			
 		}
 	}
 }

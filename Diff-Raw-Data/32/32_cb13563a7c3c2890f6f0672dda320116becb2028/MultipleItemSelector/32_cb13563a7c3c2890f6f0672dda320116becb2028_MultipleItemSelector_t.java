 package com.sun.multipleselector.impl;
 
 import java.util.Collection;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.widget.ListView;
 
 import com.sun.multipleselector.api.MultipleSelector;
 import com.sun.multipleselector.api.OnTextViewLoad;
 
 
 public class MultipleItemSelector<T> {
 	private Dialog _dialog;
 	private ListView _list;
 	private MultipleItemSelector.OnClickListener<T> _onDialogClickListener;
 	private MultipleSelector<T> _listAdapter;
 	
 	public MultipleItemSelector(Context context_, int resource_, int checkBox_,
 			int textView_, List<T> itemList_, MultipleItemSelector.OnClickListener<T> onDialogClickListener_) {
 		_listAdapter = new MultipleSelectorArrayAdapter<T>(context_, resource_, checkBox_, textView_, itemList_);
 		_list = new ListView(context_);
 		_list.setAdapter(_listAdapter);
 		_dialog = createNewDialog(context_);
 		_onDialogClickListener = onDialogClickListener_;
 	}
 	
 	
 	public MultipleItemSelector(Context context_, int resource_, int checkBox_,
 			int textView_, List<T> itemList_,  MultipleItemSelector.OnClickListener<T> onDialogClickListener_, 
 			OnTextViewLoad<T> adapterCallback_) {
 		_listAdapter = new MultipleSelectorArrayAdapter<T>(context_, resource_, checkBox_, textView_, itemList_, adapterCallback_);
 		_onDialogClickListener = onDialogClickListener_;
 		_list = new ListView(context_);
 		_list.setAdapter(_listAdapter);
 		_dialog = createNewDialog(context_);
 	}
 	
 	

 	public interface OnClickListener<T>{
 		
 		public void onNegativeClick(DialogInterface dialog, int which);
 		
 		public void onPositiveClick(Collection<T> selectedItems_, DialogInterface dialog_, int which_);
 		
 	}
 	
 	public void showDialog(){
 		_dialog.show();
 	}
 	
 	public void hideDialog(){
 		_dialog.hide();
 	}
 	
 	public void addToAdapter(Collection<T> items_){
 		_listAdapter.addAllToAdapter(items_);
 	}
 	
 	public void clearAdapter(){
 		_listAdapter.clearAdapter();
 	}
 	
 	public void remove(T object_){
 		_listAdapter.remove(object_);
 	}
 
 	protected Dialog createNewDialog(Context context_) {
 		  AlertDialog.Builder builder = new AlertDialog.Builder(context_)
 			.setView(_list).setPositiveButton(" OK ", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {	
 					if(_onDialogClickListener != null){
 					 _onDialogClickListener.onPositiveClick(_listAdapter.getAllSelectedItems(), dialog, which);
 					}
 					 
 				}
 			}).setNegativeButton(" Cancel ", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog_, int which_) {
 					if(_onDialogClickListener != null){
 					_onDialogClickListener.onNegativeClick(dialog_, which_);	
 					}
 				}
 			})
 			.setTitle("Title of dialog");
 	
 		Dialog	dialog = builder.create();
 		return dialog;
 	}
 	
 }

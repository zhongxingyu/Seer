 package com.ouchadam.fang.presentation.item;
 
 import com.ouchadam.bookkeeper.domain.ProgressValues;
 import com.ouchadam.bookkeeper.watcher.adapter.ListItemProgress;
 import com.ouchadam.bookkeeper.watcher.adapter.TypedBaseAdapter;
 import com.ouchadam.fang.Log;
 import com.ouchadam.fang.domain.FullItem;
 
 class ItemProgressManager extends ListItemProgress<FullItem, ItemAdapter.ViewHolder> {
 
     public ItemProgressManager(TypedBaseAdapter<FullItem> baseAdapter) {
         super(baseAdapter);
     }
 
     @Override
     protected void onStart(FullItem what, ItemAdapter.ViewHolder viewHolder) {
         viewHolder.title.setText("Downloading...");
         viewHolder.channelTitle.setText(what.getItem().getTitle());
     }
 
     @Override
     protected void onUpdate(FullItem what, ItemAdapter.ViewHolder viewHolder, ProgressValues progressValues) {
        viewHolder.title.setText("Downloading...");
        viewHolder.channelTitle.setText(what.getItem().getTitle());
     }
 
     @Override
     protected void onStop(FullItem what, ItemAdapter.ViewHolder viewHolder) {
         viewHolder.title.setText(what.getItem().getTitle());
         viewHolder.channelTitle.setText(what.getChannelTitle());
     }
 
 }

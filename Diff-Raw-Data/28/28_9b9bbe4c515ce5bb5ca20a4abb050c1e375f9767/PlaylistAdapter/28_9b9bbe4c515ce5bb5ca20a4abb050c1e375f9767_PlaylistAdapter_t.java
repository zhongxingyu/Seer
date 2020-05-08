 package com.ouchadam.fang.presentation.item;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.novoda.notils.caster.Views;
 import com.ouchadam.bookkeeper.domain.ProgressValues;
 import com.ouchadam.bookkeeper.watcher.ListItemWatcher;
 import com.ouchadam.bookkeeper.watcher.adapter.ListItemProgress;
 import com.ouchadam.bookkeeper.watcher.adapter.ProgressDelegate;
 import com.ouchadam.fang.Log;
 import com.ouchadam.fang.R;
 import com.ouchadam.fang.domain.FullItem;
 import com.squareup.picasso.Picasso;
 
 public class PlaylistAdapter extends TypedListAdapter<FullItem> implements ListItemWatcher.ItemWatcher {
 
     private final LayoutInflater layoutInflater;
     private final Context context;
     private final IsPlayingFetcher isPlayingFetcher;
     private final OnPlayListener onPlayListener;
     private final ProgressDelegate<ViewHolder> progressDelegate;
     private final ItemManipulator itemManipulator;
 
     public interface IsPlayingFetcher {
         boolean isPlaying(long itemId);
     }
 
     public interface OnPlayListener {
         void onPlayPause(FullItem fullItem);
     }
 
     public PlaylistAdapter(LayoutInflater layoutInflater, Context context, IsPlayingFetcher isPlayingFetcher, OnPlayListener onPlayListener, ItemManipulator itemManipulator) {
         this.layoutInflater = layoutInflater;
         this.context = context;
         this.isPlayingFetcher = isPlayingFetcher;
         this.onPlayListener = onPlayListener;
         this.itemManipulator = itemManipulator;
         this.progressDelegate = new ItemProgressManager(this);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup viewGroup) {
         View view = createView(convertView, viewGroup);
         ViewHolder viewHolder = (ViewHolder) view.getTag();
         PositionHolder positionHolder = (PositionHolder) viewHolder.playButton.getTag();
         positionHolder.position = position;
 
         ListItemProgress.Stage stage = progressDelegate.getStage(position);
         FullItem item = getItem(position);
         updateViewHolder(position, viewHolder, stage, item);
         setHolderImage(viewHolder, item.getImageUrl());
         initPlayButton(viewHolder, item);
         return view;
     }
 
     private void updateViewHolder(int position, ViewHolder viewHolder, ListItemProgress.Stage stage, FullItem item) {
         if (stage == ListItemProgress.Stage.IDLE) {
             updateIdleViewHolder(viewHolder, item);
         } else {
             progressDelegate.handleDownloadProgress(position, viewHolder);
         }
     }
 
     public void setPlaying(long itemId, boolean isPlaying) {
         try {
            int position = getPositionFor(itemId);
            if (position != -1) {
                ViewHolder viewHolder = itemManipulator.getItemViewHolder(position);
                if (viewHolder.playButton != null) {
                    viewHolder.playButton.setImageDrawable(isPlaying ? getPauseDrawable() : getPlayDrawable());
                }
             }
         } catch (ItemManipulator.ViewHolderNotFoundException e) {
             Log.e("Tried to set playing but the view holder is not available");
             e.printStackTrace();
         }
     }
 
     private void initPlayButton(ViewHolder viewHolder, FullItem item) {
         long itemId = item.getItemId();
         if (item.isDownloaded()) {
             viewHolder.playButton.setVisibility(View.VISIBLE);
             viewHolder.playButton.setImageDrawable(isPlayingFetcher.isPlaying(itemId) ? getPauseDrawable() : getPlayDrawable());
         } else {
             viewHolder.playButton.setVisibility(View.INVISIBLE);
         }
     }
 
     private Drawable getPauseDrawable() {
         return getDrawable(R.drawable.ic_pause_over_video);
     }
 
     private Drawable getPlayDrawable() {
         return getDrawable(R.drawable.ic_play_over_video);
     }
 
     private Drawable getDrawable(int id) {
         return context.getResources().getDrawable(id);
     }
 
     private View createView(View convertView, ViewGroup viewGroup) {
         return convertView == null ? createNewView(viewGroup) : convertView;
     }
 
     private View createNewView(ViewGroup viewGroup) {
         View newView = layoutInflater.inflate(R.layout.download_item_adapter, viewGroup, false);
         initViewTag(newView);
         return newView;
     }
 
     private void initViewTag(View newView) {
         ViewHolder tag = new ViewHolder();
         initHolder(newView, tag);
         newView.setTag(tag);
     }
 
     private void initHolder(View newView, ViewHolder tag) {
         tag.progressBar = (ProgressBar) newView.findViewById(R.id.progress);
         tag.title = Views.findById(newView, R.id.text);
         tag.channelTitle = Views.findById(newView, R.id.channel_title);
         tag.channelImage = Views.findById(newView, R.id.channel_image);
         tag.itemTime = Views.findById(newView, R.id.item_time);
         tag.listened = Views.findById(newView, R.id.listened);
         tag.playButton = Views.findById(newView, R.id.image_play_button);
         tag.playButton.setTag(new PositionHolder());
         tag.playButton.setOnClickListener(onClickListener);
     }
 
     private final View.OnClickListener onClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             PositionHolder position = (PositionHolder) view.getTag();
             FullItem item = getItem(position.position);
             if (item.isDownloaded()) {
                 PlaylistAdapter.this.onPlayListener.onPlayPause(item);
             }
         }
     };
 
     private void updateIdleViewHolder(ViewHolder holder, FullItem item) {
         setHolderText(holder, item);
     }
 
     private void setHolderImage(ViewHolder holder, String imageUrl) {
         if (holder.channelImage != null) {
             Picasso.with(context).load(imageUrl).resize(200, 200).centerCrop().into(holder.channelImage);
         }
     }
 
     private void setHolderText(ViewHolder holder, FullItem item) {
         holder.title.setText(item.getItem().getTitle());
         holder.channelTitle.setText(item.getChannelTitle());
         holder.itemTime.setText(item.getItem().getPubDate().getTimeAgo());
         holder.listened.setText(item.getInitialPlayPosition().asPercentage() + "%" + " listened");
         holder.progressBar.setVisibility(View.GONE);
     }
 
     @Override
     public void setStageFor(long itemId, ListItemProgress.Stage stage) {
         progressDelegate.setStageFor(itemId, stage);
     }
 
     @Override
     public void updateProgressValuesFor(long itemId, ProgressValues progressValues) {
         progressDelegate.updateProgressValuesFor(itemId, progressValues);
     }
 
     @Override
     public void notifyItem(long itemId, ListItemProgress.Stage stage) {
         try {
             handleWatcherUpdate(itemId, stage);
         } catch (ItemManipulator.ViewHolderNotFoundException e) {
             Log.e("Tried to update via download watcher but the view holder is not available");
             e.printStackTrace();
         }
     }
 
     private void handleWatcherUpdate(long itemId, ListItemProgress.Stage stage) throws ItemManipulator.ViewHolderNotFoundException {
         int position = getPositionFor(itemId);
        if (position != -1) {
            ViewHolder viewHolder = getItemViewHolder(position);
            FullItem item = getItem(position);
            updateViewHolder(position, viewHolder, stage, item);
        }
     }
 
    private ViewHolder getItemViewHolder(int position) throws ItemManipulator.ViewHolderNotFoundException {
        return itemManipulator.getItemViewHolder(position);
     }
 
     private int getPositionFor(long itemId) {
         for (int index = 0; index < getCount(); index++) {
             if (getItemId(index) == itemId) {
                 return index;
             }
         }

        return -1;
     }
 
     @Override
     public long getItemId(int position) {
         return getItem(position).getItemId();
     }
 
     public static class ViewHolder {
         TextView title;
         TextView channelTitle;
         TextView itemTime;
         TextView listened;
         ImageView playButton;
         ImageView channelImage;
         ProgressBar progressBar;
     }
 
     private static class PositionHolder {
         int position;
     }
 
 }

 /*
 This file is part of AbianReader.
 
 AbianReader is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 AbianReader is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with AbianReader.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.abiansoftware.lib.reader;
 
 import com.abiansoftware.lib.reader.R;
 import com.abiansoftware.lib.reader.AbianReaderData.AbianReaderItem;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 class AbianReaderListView extends LinearLayout implements OnClickListener,
 		OnTouchListener {
 	private static final String TAG = "AbianReaderListView";
 
 	private ListView m_abianReaderListView;
 	private AbianReaderListAdapter m_abianReaderListAdapter;
 
 	private RelativeLayout m_footerView;
 	private ImageView m_footerPrevImage;
 	private ImageView m_footerNextImage;
 
 	private RelativeLayout m_headerView;
 	private ImageView m_headerImageView;
 	private TextView m_headerTextView;
 	private TextView m_headerTitleTextView;
 	private TextView m_headerCountTextView;
 	private ProgressBar m_headerProgressBar;
 
 	private int m_touchStartX;
 
 	private Runnable m_gotoNextFeaturedArticleRunnable;
 
 	public AbianReaderListView(Context context) {
 		super(context);
 
 		initializeViewBeforePopulation(context);
 	}
 
 	public AbianReaderListView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		initializeViewBeforePopulation(context);
 	}
 
 	private void initializeViewBeforePopulation(Context context) {
 		m_abianReaderListView = null;
 		m_abianReaderListAdapter = null;
 
 		m_headerView = null;
 		m_headerImageView = null;
 		m_headerTextView = null;
 
 		m_footerView = null;
 		m_footerPrevImage = null;
 		m_footerNextImage = null;
 
 		m_gotoNextFeaturedArticleRunnable = new Runnable() {
 			public void run() {
 				AbianReaderData theData = AbianReaderActivity.getData();
 
 				if (theData != null) {
 					theData.nextFeaturedArticle();
 					updateList(false);
 				}
 			}
 		};
 
 		m_touchStartX = 0;
 	}
 
 	public void initializeViewAfterPopulation(Context context) {
 		AbianReaderActivity theSingleton = AbianReaderActivity.getSingleton();
 
 		m_abianReaderListView = (ListView) AbianReaderActivity.getSingleton()
				.findViewById(R.id.abian_reader_list_view);
 
 		m_abianReaderListView.setVisibility(View.GONE);
 
 		m_abianReaderListAdapter = new AbianReaderListAdapter(theSingleton);
 
 		int preferredListItemHeight = theSingleton.getPreferredListItemHeight();
 
 		LayoutInflater theLayoutInflater = LayoutInflater.from(context);
 
 		m_headerView = (RelativeLayout) theLayoutInflater.inflate(
 				R.layout.abian_reader_list_header, null);
 		m_headerImageView = (ImageView) m_headerView
 				.findViewById(R.id.abian_reader_list_header_image_view);
 		m_headerTextView = (TextView) m_headerView
 				.findViewById(R.id.abian_reader_list_header_text_view);
 		m_headerProgressBar = (ProgressBar) m_headerView
 				.findViewById(R.id.abian_reader_list_header_progress_bar);
 		m_headerTitleTextView = (TextView) m_headerView
 				.findViewById(R.id.abian_reader_list_header_title_text_view);
 		m_headerCountTextView = (TextView) m_headerView
 				.findViewById(R.id.abian_reader_list_header_count_text_view);
 
 		m_headerTitleTextView.setText(theSingleton.getFeaturedTag()
 				.toUpperCase());
 
 		m_headerProgressBar.setIndeterminate(true);
 
 		m_headerProgressBar.setVisibility(View.GONE);
 		m_headerView.setVisibility(View.GONE);
 		m_abianReaderListView.addHeaderView(m_headerView);
 		m_headerView.setOnClickListener(this);
 		m_headerView.setOnTouchListener(this);
 
 		m_footerView = (RelativeLayout) theLayoutInflater.inflate(
 				R.layout.abian_reader_list_footer, null);
 		m_footerPrevImage = (ImageView) m_footerView
 				.findViewById(R.id.abian_reader_list_footer_prev_image_view);
 		m_footerNextImage = (ImageView) m_footerView
 				.findViewById(R.id.abian_reader_list_footer_next_image_view);
 
 		m_footerView.setVisibility(View.GONE);
 		m_abianReaderListView.addFooterView(m_footerView);
 
 		m_footerPrevImage.setOnClickListener(this);
 		m_footerNextImage.setOnClickListener(this);
 
 		m_abianReaderListView.setAdapter(m_abianReaderListAdapter);
 
 		m_footerView.setClickable(false);
 		m_footerView.setFocusable(false);
 		m_footerView.setFocusableInTouchMode(false);
 
 		/*
 		 * ViewGroup.LayoutParams headerLayoutParams =
 		 * m_headerView.getLayoutParams(); headerLayoutParams.height =
 		 * preferredListItemHeight*2;
 		 * m_headerView.setLayoutParams(headerLayoutParams);
 		 */
 
 		Log.e(TAG, "Preferred Item height = " + preferredListItemHeight);
 
 		ViewGroup.LayoutParams headerTitleTextLayoutParams = m_headerTitleTextView
 				.getLayoutParams();
 		headerTitleTextLayoutParams.height = ((headerTitleTextLayoutParams.height * 3) / 8);
 		m_headerTitleTextView.setLayoutParams(headerTitleTextLayoutParams);
 
 		ViewGroup.LayoutParams headerCountTextLayoutParams = m_headerCountTextView
 				.getLayoutParams();
 		headerCountTextLayoutParams.height = ((headerCountTextLayoutParams.height * 3) / 8);
 		m_headerCountTextView.setLayoutParams(headerCountTextLayoutParams);
 
 		ViewGroup.LayoutParams headerImageLayoutParams = m_headerImageView
 				.getLayoutParams();
 		headerImageLayoutParams.height = headerImageLayoutParams.height * 2;
 		m_headerImageView.setLayoutParams(headerImageLayoutParams);
 
 		ViewGroup.LayoutParams headerTextLayoutParams = m_headerTextView
 				.getLayoutParams();
 		headerTextLayoutParams.height = ((headerTextLayoutParams.height * 3) / 4);
 		m_headerTextView.setLayoutParams(headerTextLayoutParams);
 
 		/*
 		 * ViewGroup.LayoutParams footerLayoutParams =
 		 * m_footerView.getLayoutParams(); footerLayoutParams.height =
 		 * preferredListItemHeight;
 		 * m_footerView.setLayoutParams(footerLayoutParams);
 		 */
 
 		ViewGroup.LayoutParams nextImageLayoutParams = m_footerNextImage
 				.getLayoutParams();
 		nextImageLayoutParams.height = ((nextImageLayoutParams.height / 10) * 8);
 		m_footerNextImage.setLayoutParams(nextImageLayoutParams);
 
 		ViewGroup.LayoutParams prevImageLayoutParams = m_footerPrevImage
 				.getLayoutParams();
 		prevImageLayoutParams.height = ((prevImageLayoutParams.height / 10) * 8);
 		m_footerPrevImage.setLayoutParams(prevImageLayoutParams);
 
 		m_headerView.setVisibility(View.GONE);
 		m_footerView.setVisibility(View.GONE);
 	}
 
 	@Override
 	public void setVisibility(int visibility) {
 		if (visibility == View.VISIBLE) {
 			startNextFeatureTimer();
 		} else {
 			stopNextFeatureTimer();
 		}
 
 		super.setVisibility(visibility);
 	}
 
 	private void startNextFeatureTimer() {
 		stopNextFeatureTimer();
 		postDelayed(m_gotoNextFeaturedArticleRunnable, 5000);
 	}
 
 	private void stopNextFeatureTimer() {
 		removeCallbacks(m_gotoNextFeaturedArticleRunnable);
 	}
 
 	public void updateList(boolean bClear) {
 		m_abianReaderListAdapter.notifyDataSetChanged();
 
 		if (bClear) {
 			stopNextFeatureTimer();
 		}
 
 		AbianReaderActivity theSingleton = AbianReaderActivity.getSingleton();
 		AbianReaderData theData = AbianReaderActivity.getData();
 
 		if (theSingleton.isRefreshingFeed()) {
 			stopNextFeatureTimer();
 
 			m_abianReaderListView.setVisibility(View.GONE);
 
 			m_headerView.setVisibility(View.GONE);
 			m_headerImageView.setVisibility(View.GONE);
 			m_headerTextView.setVisibility(View.GONE);
 			m_footerView.setVisibility(View.GONE);
 		} else {
 			boolean bWasGone = (m_abianReaderListView.getVisibility() == View.GONE);
 
 			m_abianReaderListView.setVisibility(View.VISIBLE);
 
 			if (bWasGone) {
 				m_abianReaderListView.post(new Runnable() {
 					public void run() {
 						m_abianReaderListView.setSelection(0);
 					}
 				});
 			}
 
 			if (theData.getNumberedOfFeaturedArticles() <= 0) {
 				m_headerView.setVisibility(View.GONE);
 				m_headerImageView.setVisibility(View.GONE);
 				m_headerTextView.setVisibility(View.GONE);
 				m_headerCountTextView.setVisibility(View.GONE);
 				m_headerTitleTextView.setVisibility(View.GONE);
 			} else {
 				if (theData != null) {
 					AbianReaderItem targetItem = theData.getFeaturedItem();
 
 					if (targetItem != null) {
 						startNextFeatureTimer();
 
 						m_headerView.setVisibility(View.VISIBLE);
 						m_headerImageView.setVisibility(View.VISIBLE);
 						m_headerTextView.setVisibility(View.VISIBLE);
 						m_headerCountTextView.setVisibility(View.VISIBLE);
 						m_headerTitleTextView.setVisibility(View.VISIBLE);
 
 						m_headerTextView.setText(targetItem.getTitle());
 
 						m_headerCountTextView
 								.setText(""
 										+ theData
 												.getFeaturedItemPositionInFeaturedList()
 										+ " of "
 										+ theData
 												.getNumberedOfFeaturedArticles());
 
 						if (targetItem.getFeaturedImageBitmap() != null) {
 							m_headerProgressBar.setVisibility(View.GONE);
 							m_headerImageView.setImageBitmap(targetItem
 									.getFeaturedImageBitmap());
 
 							ViewGroup.LayoutParams headerLayoutParams = m_headerView
 									.getLayoutParams();
 							headerLayoutParams.height = 0;
 							m_headerView.setLayoutParams(headerLayoutParams);
 						} else {
 							m_headerImageView.setImageBitmap(null);
 							m_headerProgressBar.setVisibility(View.VISIBLE);
 
 							ViewGroup.LayoutParams headerLayoutParams = m_headerView
 									.getLayoutParams();
 							headerLayoutParams.height = (int) (AbianReaderActivity
 									.getSingleton()
 									.getPreferredListItemHeight() * 2.5f);
 							m_headerView.setLayoutParams(headerLayoutParams);
 							// m_headerImageView.setImageResource(R.drawable.loading_gif);
 						}
 					}
 				}
 			}
 
 			m_footerView.setVisibility(View.VISIBLE);
 		}
 
 		if (theData.getPageNumber() == 1) {
 			m_footerPrevImage.setVisibility(View.GONE);
 		} else {
 			m_footerPrevImage.setVisibility(View.VISIBLE);
 		}
 
 		if (theData.getNumberOfItems() == 0) {
 			m_footerNextImage.setVisibility(View.GONE);
 		} else {
 			m_footerNextImage.setVisibility(View.VISIBLE);
 		}
 	}
 
 	private static class RSSFeedListItem {
 		TextView m_titleText;
 		TextView m_detailsText;
 		ImageView m_imageView;
 
 		int m_targetIndex;
 
 		public RSSFeedListItem() {
 			resetData();
 		}
 
 		public void resetData() {
 			m_targetIndex = 0;
 		}
 	}
 
 	private static class AbianReaderListAdapter extends BaseAdapter implements
 			OnClickListener {
 		private LayoutInflater m_layoutInflater;
 
 		public AbianReaderListAdapter(Context context) {
 			m_layoutInflater = LayoutInflater.from(context);
 		}
 
 		public int getCount() {
 			if (AbianReaderActivity.getSingleton().isRefreshingFeed()) {
 				return 0;
 			}
 
 			AbianReaderData rssData = AbianReaderActivity.getData();
 
 			if (rssData.getNumberOfItems() == 0) {
 				// return 1;
 			}
 
 			return rssData.getNumberOfItems();
 		}
 
 		public Object getItem(int position) {
 			return position;
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			RSSFeedListItem listItem = null;
 
 			if (convertView == null) {
 				convertView = m_layoutInflater.inflate(
 						R.layout.abian_reader_list_item, null);
 
 				listItem = new RSSFeedListItem();
 				listItem.m_titleText = (TextView) convertView
 						.findViewById(R.id.abian_reader_list_item_title_text_view);
 				listItem.m_detailsText = (TextView) convertView
 						.findViewById(R.id.abian_reader_list_item_details_text_view);
 				listItem.m_imageView = (ImageView) convertView
 						.findViewById(R.id.abian_reader_list_item_icon_image_view);
 
 				convertView.setTag(listItem);
 			} else {
 				listItem = (RSSFeedListItem) convertView.getTag();
 				listItem.resetData();
 			}
 
 			AbianReaderData theData = AbianReaderActivity.getData();
 
 			if ((position == 0) & (theData.getNumberOfItems() == 0)) {
 				listItem.m_targetIndex = -1;
 				listItem.m_titleText.setText("No articles found");
 				listItem.m_detailsText.setText("Click here to try again");
 			} else {
 				AbianReaderItem theItem = theData.getItemNumber(position);
 
 				listItem.m_targetIndex = position;
 				listItem.m_titleText.setText(theItem.getTitle());
 
 				String detailsText = theItem.getPubDateOnly();
 				detailsText += ", ";
 
 				if (theItem.getCommentsCount() == 0) {
 					detailsText += "No";
 				} else {
 					detailsText += Integer.toString(theItem.getCommentsCount());
 				}
 
 				detailsText += " Comment";
 
 				if (theItem.getCommentsCount() != 1) {
 					detailsText += "s";
 				}
 
 				listItem.m_detailsText.setText(detailsText);
 
 				if (theItem.getThumbnailBitmap() != null) {
 					listItem.m_imageView.setImageBitmap(theItem
 							.getThumbnailBitmap());
 				} else {
 					listItem.m_imageView.setImageResource(R.drawable.app_icon);
 				}
 			}
 
 			convertView.setTag(listItem);
 
 			convertView.setOnClickListener(this);
 
 			return convertView;
 		}
 
 		public void onClick(View v) {
 			RSSFeedListItem listItem = (RSSFeedListItem) v.getTag();
 
 			if (listItem != null) {
 				AbianReaderActivity.getSingleton().showRssItemContent(
 						listItem.m_targetIndex);
 			}
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.abian_reader_list_footer_next_image_view) {
 			updateList(true);
 			AbianReaderActivity.getSingleton().getNextFeed();
 		} else if (v.getId() == R.id.abian_reader_list_footer_prev_image_view) {
 			updateList(true);
 			AbianReaderActivity.getSingleton().getPrevFeed();
 		}
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		if (v.getId() == R.id.abian_reader_list_header_layout) {
 			if (event.getAction() == MotionEvent.ACTION_DOWN) {
 				stopNextFeatureTimer();
 				m_touchStartX = (int) event.getX();
 			} else if ((event.getAction() == MotionEvent.ACTION_UP)
 					|| (event.getAction() == MotionEvent.ACTION_CANCEL)) {
 				int changeInX = (m_touchStartX - (int) event.getX());
 
 				Log.e(TAG, "Change in X: " + changeInX);
 
 				if (changeInX < -3) {
 					AbianReaderActivity.getData().previousFeaturedArticle();
 					updateList(false);
 				} else if (changeInX > 3) {
 					AbianReaderActivity.getData().nextFeaturedArticle();
 					updateList(false);
 				} else if (event.getAction() == MotionEvent.ACTION_UP) {
 					int featuredPosition = AbianReaderActivity.getData()
 							.getFeaturedItemPositionInCompleteList();
 					AbianReaderActivity.getSingleton().showRssItemContent(
 							featuredPosition);
 				} else {
 					startNextFeatureTimer();
 				}
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 }

 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.communicator.fragments.channels;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.communicator.HomeActivity;
 import eu.trentorise.smartcampus.communicator.R;
 import eu.trentorise.smartcampus.communicator.custom.AbstractAsyncTaskProcessor;
 import eu.trentorise.smartcampus.communicator.custom.ChannelAdapter;
 import eu.trentorise.smartcampus.communicator.custom.HelpDlgHelper;
 import eu.trentorise.smartcampus.communicator.custom.data.CommunicatorHelper;
 import eu.trentorise.smartcampus.communicator.model.Channel;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 
 public class ChannelListFragment extends SherlockListFragment {
 
         @Override
         public void onCreate(Bundle savedInstanceState) {
                 super.onCreate(savedInstanceState);
                 setHasOptionsMenu(true);
         }
         
         @Override
         public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
                 return arg0.inflate(R.layout.channels, arg1, false);
         }
 
         protected boolean isFeed() {
                 return false;
         }
         
         @Override
         public void onStart() {
                 super.onStart();
 
 
                 getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
                 getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                 getSherlockActivity().setTitle(isFeed() ? R.string.title_feeds : R.string.title_channels);
 
                 
                 ChannelAdapter adapter = new ChannelAdapter(getActivity(), R.layout.channel, CommunicatorHelper.getChannels(isFeed()));
                 setListAdapter(adapter);
                 getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
                         @Override
                         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                 viewChannelMessages(((ChannelAdapter)getListAdapter()).getItem(position));
                         }
                 });
                 
                 registerForContextMenu(getListView());
                 if (isFeed()) HelpDlgHelper.helpHint(getSherlockActivity(), R.string.help_feeds, "feeds");
                 else HelpDlgHelper.helpHint(getSherlockActivity(), R.string.help_channels, "channels");
         }
 
         
         
         @Override
         public void onPrepareOptionsMenu(Menu menu) {
                 menu.clear();
                 getSherlockActivity().getSupportMenuInflater().inflate(R.menu.channel_list_menu, menu);
 //                MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.channel_add, 1, R.string.channel_add);
 //                item.setIcon(R.drawable.add);
 //                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 //                super.onPrepareOptionsMenu(menu);
         }
 
         @Override
         public boolean onOptionsItemSelected(MenuItem item) {
                 switch (item.getItemId()) {
                 case R.id.channel_add:
                         openChannelForm(null);
                         return true;
                 case R.id.channels_help:
                         HelpDlgHelper.help(getSherlockActivity(), isFeed() ? R.string.help_feeds : R.string.help_channels);
                         return true;
                 default:
                         return super.onOptionsItemSelected(item);
                 }
         }
 
         @Override
         public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
                 menu.setHeaderTitle(isFeed() ? R.string.feed_options_header : R.string.channel_options_header);
                 android.view.MenuInflater inflater = getSherlockActivity().getMenuInflater();
             inflater.inflate(R.menu.channel_menu, menu);
         }
 
         @Override
         public boolean onContextItemSelected(android.view.MenuItem item) {
                 AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                 Channel content = ((ChannelAdapter)getListAdapter()).getItem(info.position);
                 return handleMenuItem(content, item.getItemId());
         }
 
         protected boolean handleMenuItem(final Channel content, int itemId) {
                 switch (itemId) {
                 case R.id.channel_option_edit:
                         openChannelForm(content);
                         return true;
                 case R.id.channel_option_remove:
                         AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
                         builder.setMessage(isFeed() ? R.string.feed_remove_text : R.string.channel_remove_text)
                                .setCancelable(false)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                            new SCAsyncTask<Channel, Void, Channel>(getActivity(), new RemoveChannelProcessor(getActivity())).execute(content);
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                         dialog.cancel();
                                    }
                                });
                         AlertDialog alert = builder.create();
                         alert.show();
                         return true;
                 case R.id.channel_option_view:
                         viewChannelMessages(content);
                         return true;
                 default:
                         return false;
                 }
         }
 
         protected void viewChannelMessages(Channel content) {
                 FragmentTransaction ft  = getSherlockActivity().getSupportFragmentManager().beginTransaction();
                 Fragment fragment = new ChannelViewFragment();
                 Bundle args = new Bundle();
                 args.putSerializable(ChannelViewFragment.ARG_CHANNEL, content);
                 fragment.setArguments(args);
                 // Replacing old fragment with new one
                 ft.replace(R.id.fragment_container, fragment);
                 ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                 ft.addToBackStack(null);
                 ft.commit();
         }
 
         protected void openChannelForm(Channel content) {
                 FragmentTransaction ft  = getSherlockActivity().getSupportFragmentManager().beginTransaction();
                 Fragment fragment = new ChannelFormFragment();
                 Bundle b = ChannelFormFragment.prepareArgs(isFeed(), content);
 //                if (content != null) {
 //                        Bundle args = new Bundle();
 //                        args.putSerializable(ChannelFormFragment.ARG_CHANNEL, CommunicatorHelper.getChannel(content.getId()));
 //                        fragment.setArguments(args);
 //                }
                 fragment.setArguments(b);
                 // Replacing old fragment with new one
                 ft.replace(R.id.fragment_container, fragment);
                 ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                 ft.addToBackStack(null);
                 ft.commit();
         }
 
         private class RemoveChannelProcessor extends AbstractAsyncTaskProcessor<Channel, Channel> {
 
                 public RemoveChannelProcessor(Activity activity) {
                         super(activity);
                 }
 
                 @Override
                 public Channel performAction(Channel... params) throws SecurityException, Exception {
                         if (CommunicatorHelper.removeChannel(params[0])) return params[0];
                         return null;
                 }
 
                 @Override
                 public void handleResult(Channel result) {
                         if (result != null) {
                                    ((ChannelAdapter)getListAdapter()).remove(result);
                                    ((ChannelAdapter)getListAdapter()).notifyDataSetChanged();
                         }
                 }
                 
         } 
         
 }

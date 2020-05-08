 /*
  *   LogCap: Capture system logs to files.
  *   Copyright (C) 2012  Jason Tian
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jasontian.logcap.util;
 
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import org.jasontian.logcap.R;
 
 import java.io.File;
 
 /**
  * @author Jason Tian
  */
 public class FileInfoPreference extends DialogPreference {
 
     private FileInfoAdapter mAdapter;
 
     public FileInfoPreference(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     @Override
     public void onClick(DialogInterface dialog, int which) {
         switch (which) {
             case -2:
                 break;
             case -1:
                 File[] logs = mAdapter.logfiles;
                 if (logs != null) {
                     for (File log : logs) {
                        Util.clearLogFile(log);
                     }
                 }
                 break;
             default:
                Util.clearLogFile((File) mAdapter.getItem(which));
         }
         setSummary(Util.getLogFileInfo(getContext()));
     }
 
     @Override
     protected void onPrepareDialogBuilder(Builder builder) {
         builder.setTitle(R.string.file_info_title);
         builder.setPositiveButton(R.string.clear_all, this);
         builder.setNegativeButton(R.string.close, this);
         builder.setAdapter(mAdapter = new FileInfoAdapter(), this);
     }
 
     private class FileInfoAdapter extends BaseAdapter {
 
         private File[] logfiles;
 
         public FileInfoAdapter() {
             logfiles = Util.getLogFiles();
         }
 
         @Override
         public int getCount() {
             return logfiles == null ? 0 : logfiles.length;
         }
 
         @Override
         public Object getItem(int position) {
             return logfiles == null ? null : logfiles[position];
         }
 
         @Override
         public long getItemId(int position) {
             return position;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 convertView = ((LayoutInflater) getContext().getSystemService(
                         Context.LAYOUT_INFLATER_SERVICE))
                         .inflate(R.layout.general_text, null);
             }
             if (logfiles != null) {
                 File log = logfiles[position];
                 if (log.isFile()) {
                     ((TextView) convertView).setText("File name: " + log.getAbsolutePath()
                             + "\nFile size: " + log.length() + " bytes");
                 }
             }
             return convertView;
         }
     }
 }

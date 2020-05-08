 /*
  * Copyright (c) 2013. Arnav Gupta
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License, version 3, as
  *     published by
  *     the Free Software Foundation.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package in.ac.iiitd.esya.fragments;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 
 /**
  * Created by championswimmer on 25/7/13.
  */
 public class EventDialog extends DialogFragment {
     public int mLayoutId;
 
     public EventDialog (int layoutId) {
         mLayoutId = layoutId;
     }
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         // Use the Builder class for convenient dialog construction
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         // Get the layout inflater
         LayoutInflater inflater = getActivity().getLayoutInflater();
 
         // Inflate and set the layout for the dialog
         // Pass null as the parent view because its going in the dialog layout

        builder.setView(inflater.inflate(mLayoutId, null));
        setRetainInstance(true);
         // Create the AlertDialog object and return it
         return builder.create();
     }
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setOnDismissListener(null);
        super.onDestroyView();
    }
 }

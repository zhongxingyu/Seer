 package com.example.sprint;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Filter;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class PrinterListAdapter extends ArrayAdapter<Printer> {
 
 	// Variables
 	private String storageFileName = "sprintapp.txt";
 	private int resource;
 	private LayoutInflater inflater;
 	private Context context;
 	private Filter filter;
 	private int recentPrinterCount=0;
 	private ArrayList<Printer> recentPrinters;
 	private ArrayList<Printer> printerListOriginal;
 	private ArrayList<Printer> printerListFiltered;
 
 	// Constructor
 	public PrinterListAdapter(Context ctx, int resourceId,
 			ArrayList<Printer> printers) {
 		super(ctx, resourceId, printers);
 		
 		//get Recent Printers
 		printerListOriginal= new ArrayList<Printer>();
 		printerListFiltered = new ArrayList<Printer>();
 		recentPrinters= getRecentPrintersArray();
 		
 		recentPrinterCount = recentPrinters.size();
 		
 		printerListOriginal.addAll(recentPrinters);
 		printerListOriginal.addAll(printers);
 		printerListFiltered.addAll(recentPrinters);
 		printerListFiltered.addAll(printers);
 		
 		//printerListFiltered = new ArrayList<Printer>(printers);
 		resource = resourceId;
 		inflater = LayoutInflater.from(ctx);
 		context = ctx;
 	}
 	
 	//Return printer list of recent printers.
 	public ArrayList<Printer> getRecentPrintersArray()
 	{
 		FileInputStream fis;
 		ArrayList<Printer> retVal=new ArrayList<Printer>();
 		String content="";
 		try 
 		{
 			//Create file, if exists gather all printers in a string.
 			File file = this.getContext().getFileStreamPath(storageFileName);
 			if(file.exists())
 			{
 				fis = this.getContext().openFileInput(storageFileName);
 				byte[] input = new byte[fis.available()];
 				while (fis.read(input) != -1 && fis.read(input)!=0) 
 				{
 					content += new String(input);
 				}
 			}
 		}
 		catch (FileNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace(); 
 		}
 		//turn the string into an array list of printers, returns recent printers
 		String[] printers = content.split(";");
 		for(int i=0;i<printers.length&& printers[i]!="";i++)
 		{
 			retVal.add(new Printer(printers[i],"",false));
 		}
 		return retVal;
 	}
 	
 	//Return concatenated list of recent printers. Separated by ';'
 	public String getRecentPrinters()
 	{
 		FileInputStream fis;
 		String content = "";
 		 
 		try 
 		{
 			//Create file & if it exists return the recent printers
 			File file = this.getContext().getFileStreamPath(storageFileName);
 			if(file.exists())
 			{
 				fis = this.getContext().openFileInput(storageFileName);
 				byte[] input = new byte[fis.available()];
 				while (fis.read(input) != -1 && fis.read(input)!=0) 
 				{
 					content += new String(input);
 				}
 			}
 		}
 		catch (FileNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace(); 
 		}
 		return content;
 	}
 	
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent) {
 
 		/* create a new view of my layout and inflate it in the row */
 		convertView = (RelativeLayout) inflater.inflate(resource, null);
 		convertView.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
             	
             	// If we don't have the job yet go to the job selection
             	if(((PrinterListActivity)context).getJobId() == null)
             	{
             		// When a printer is clicked show the job list and send the printer selected
                     Intent intent = new Intent(context, JobListActivity.class);
             		intent.putExtra("printer_name", printerListFiltered.get(position).getName());
             		intent.putExtra("source", "printerlist");
             		context.startActivity(intent);
             	}
             	else
             	{
             		// Start the confirmation activity            	
                 	Intent intent = new Intent(context, PrintConfirmationActivity.class);
                 	intent.putExtra("printer_name", printerListFiltered.get(position).getName());
             		intent.putExtra("job_id", ((PrinterListActivity)context).getJobId());
             		intent.putExtra("job_name", ((PrinterListActivity)context).getJobName());
             		intent.putExtra("job_type", ((PrinterListActivity)context).getJobType());
             		intent.putExtra("document_owner", ((PrinterListActivity)context).getDocumentOwner());
             		context.startActivity(intent);
             	}
             	
             }
         });
 		
 		/* Get the printer object from the filtered list position*/
 		Printer printer = printerListFiltered.get(position);
 
 		/* Set the printer's name*/
 		TextView title = (TextView) convertView
 				.findViewById(R.id.tvPrinterTitle);
 		String hrPrinterName = makePrinterNameHumanReadable(printer.getName());
 		title.setText(hrPrinterName);
 
 		/* Set the printer's location */
 		TextView location = (TextView) convertView
 				.findViewById(R.id.tvPrinterLocation);
 		location.setText(printer.getLocation());
 
 		/* Set the printer image based on the type of printer */
 		ImageView imagePrinter = (ImageView) convertView
 				.findViewById(R.id.imgPrinter);
 		
 		String uri = "";
 		if(printer.getColor())
 			uri = "drawable/color_printer";
 		else
 			uri = "drawable/blackwhite_printer";
 
 		int imageResource = context.getResources().getIdentifier(uri, null,
 				context.getPackageName());
 		Drawable image = context.getResources().getDrawable(imageResource);
 		imagePrinter.setImageDrawable(image);
 		
 		
 		int sectionHeaderVisible = View.GONE;
 		String sectionHeaderText ="";
 		
 		if(position==0 && recentPrinters.size()>0 && printerListFiltered.size() == printerListOriginal.size())
 		{
 			sectionHeaderVisible=View.VISIBLE;
 			sectionHeaderText ="Recent Printers";
 
 		}
 		else if(position == recentPrinterCount && printerListFiltered.size() == printerListOriginal.size())
 		{
 			sectionHeaderVisible=View.VISIBLE;
 			sectionHeaderText ="All Printers";
 		}
 
 		TextView separator = (TextView) convertView.findViewById(R.id.separator);
 		separator.setText(sectionHeaderText);
 		separator.setVisibility(sectionHeaderVisible);		
 
 		return convertView;
 	}
 
 	private String makePrinterNameHumanReadable(String codedName) {
 
 		//if the scan was not successful, or returned a string longer or shorter than expected
 		if(codedName == null || codedName.length() < 6) {
 			return "";
 		}
 
 		String humanReadableName = "";
 
 		//Set the building wing
		if( codedName.substring(0, 1).toLowerCase(Locale.getDefault()).contentEquals("w") ) {
 			//woodward
 			humanReadableName = "Woodward, ";
 		} else {
 			if( codedName.substring(0, 1).contentEquals("m") ) {
 				//monroe
 				humanReadableName = "Monroe, ";
 			} else {
 				if( codedName.substring(0, 1).contentEquals("c") ) {
 					//center
 					humanReadableName = "Center, ";
 				} else { return "";}
 			}
 		}
 
 		//set the floor
 		String sFloor = codedName.substring(1, 3);
 		int floor = 0;
 		try {
 			floor = Integer.parseInt(sFloor);
 		} catch(Exception e) {
 			Log.v("makePrinterNameHumanReadable-floor", e.getLocalizedMessage());
 		}
 
 		if(floor == 0) {
 			//no-op.  keep human readable floor without adding a value since Integer.parse failed. 
 		} else {
 			humanReadableName = floor + " " + humanReadableName;
 		}
 
 		//set the printer number
 
 		//set the floor
 		String sPrinter = codedName.substring(4, 6);
 		int printer = 0;
 		try {
 			printer = Integer.parseInt(sPrinter);
 		} catch(Exception e) {
 			Log.v("makePrinterNameHumanReadable-printer", e.getLocalizedMessage());
 		}
 
 		if(printer == 0) {
 			//no-op.  keep human readable floor without adding a value since Integer.parse failed. 
 		} else {
 			humanReadableName = humanReadableName + "Printer " + printer;
 		}
 
 
 		return humanReadableName;
 	}
 	
 	@Override
 	public Filter getFilter() {
 		/* Get a custom filter for the adapter */
 		if (filter == null)
 			filter = new PrinterListFilter();
 
 		return filter;
 	}
 	
 	@Override
 	public int getCount(){
 		/* Get the count based on the filtered printer list size*/
 		return printerListFiltered.size();
 	}
 
 	/*
 	 * class: PrinterListFilter
 	 * description: Custom Filter for printers.  The filter is implemented
 	 * 				to allow filtering on different fields of the printer
 	 * 				object such as 'name' and 'location'
 	 */
 	private class PrinterListFilter extends Filter {
 		@Override
 		protected FilterResults performFiltering(CharSequence constraint) {
 
 			FilterResults results = new FilterResults();
 
 			if (constraint == null || constraint.length() == 0) {
 				/* No filter. Return the whole list*/
 				results.values = printerListOriginal;
 				results.count = printerListOriginal.size();
 				
 			} else {
 				/* Filtering*/
 				ArrayList<Printer> nPrinterList = new ArrayList<Printer>();
 				for (Printer p : printerListOriginal) {
 					/* Compare the upper-case of the printer name with the text input */
 					if (p.getName().toUpperCase(Locale.getDefault())
 							.contains(constraint.toString().toUpperCase(Locale.getDefault())) ||
 							makePrinterNameHumanReadable(p.getName().toUpperCase(Locale.getDefault()))
 							.contains(constraint.toString().toUpperCase(Locale.getDefault()))) {
 						
 						/* Add to the new filtered printer list */
 						nPrinterList.add(p);
 					}
 				}
 				results.values = nPrinterList;
 				results.count = nPrinterList.size();
 			}
 			return results;
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		protected void publishResults(CharSequence constraint,
 				FilterResults results) {
 			if (results.count == 0) {
 				/* No results matched the text input, so show the whole list */
 				//printerListFiltered = printerListOriginal;
 				// Show nothing
 				printerListFiltered.clear();
 				notifyDataSetChanged();
 			} else {
 				/* There are matching results.  Show only those which match */
 				printerListFiltered = (ArrayList<Printer>) results.values;
 				notifyDataSetChanged();
 			}
 		}
 	}
 }

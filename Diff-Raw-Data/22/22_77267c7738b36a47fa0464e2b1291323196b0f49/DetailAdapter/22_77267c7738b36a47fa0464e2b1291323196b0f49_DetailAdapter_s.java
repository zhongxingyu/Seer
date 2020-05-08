 package com.hutchdesign.transitgenie;
 
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class DetailAdapter extends BaseAdapter implements OnClickListener {
     private Context context;
     private Activity parentActivity;
     private NodeList stepList;
     private String prevLocation;	//Last location as specified by previous Node.
     
     public DetailAdapter(Context context, Activity parentActivity, NodeList stepList) {
         this.context = context;
         this.parentActivity = parentActivity;
         this.stepList = stepList;
         this.prevLocation = "";
     }
 
     public int getCount() {
         return stepList.getLength();
     }
 
     public Object getItem(int position) {
         return stepList.item(position);
     }
 
     public long getItemId(int position) {
         return position;
     }
     
     public View getView(int position, View convertView, ViewGroup viewGroup) {
     	
         Node curr = stepList.item(position);
         
         if (convertView == null) {
             LayoutInflater inflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = inflater.inflate(R.layout.detail, null);
         }
         
         //Import widgets needed from detail.xml
         ImageView stepImage = (ImageView) convertView.findViewById(R.id.detail_step);	//Image of walking, bus, or train color
         TextView stepTag = (TextView) convertView.findViewById(R.id.detail_length);  	//Image Tag (tag = walk length OR bus number OR train line)
         
         TextView stepFrom = (TextView) convertView.findViewById(R.id.detail_text_from);	//Starting point for this step
         TextView stepTo = (TextView) convertView.findViewById(R.id.detail_text_to);		//Arrival point for this step
         TextView stepFor = (TextView) convertView.findViewById(R.id.detail_text_for);	//Length of step OR board & alight times
         TextView stepForTitle = (TextView) convertView.findViewById(R.id.detail_title_for); //default: "FOR", change to "BOARD" for bus/train
         
        
         //SET IMAGE AND TAG  -----------------------------------------------------------------------
         //(tag = walk length OR bus number OR train line)
         
         String nodeName = curr.getNodeName();		//Get name of Node (e.g. "walk" or "transit")
         NamedNodeMap attr = curr.getAttributes();	//Get current Node's attributes
 
         if(nodeName.equals("transit")) //public transit node
 		{
         	//Change label "FOR" to "BOARD"
         	//(walk nodes display "FOR x minutes" ...
         	//transit nodes display "BOARD 00:00 ALIGHT 00:00")
         	stepForTitle.setText("BOARD");
         	
         	//Import widgets to set alight time
         	TextView stepAlightTitle = (TextView) convertView.findViewById(R.id.detail_title_alight);
         	TextView stepAlight = (TextView) convertView.findViewById(R.id.detail_text_alight);
         	TableRow row1 =(TableRow) convertView.findViewById(R.id.detail_row);
         	stepAlightTitle.setVisibility(0);	//Set the label "ALIGHT" to visible
         	stepAlight.setVisibility(0);
         	row1.setVisibility(0);
         	
         	long board = Long.parseLong(attr.item(9).getNodeValue());	//Board Time
         	long alight = Long.parseLong(attr.item(15).getNodeValue()); //Alight Time
         	stepFor.setText(Routes.formatMillis(board));
         	stepAlight.setText(Routes.formatMillis(alight));
         	
 			String temp = attr.item(0).getNodeValue();	//Agency Id
 			String type = attr.item(1).getNodeValue();	//Route type
 			String rtid = attr.item(2).getNodeValue();  //Route Id
 			
 			stepFrom.setText(attr.item(6).getNodeValue());	//"From" is attribute board_stop
 			prevLocation = attr.item(14).getNodeValue();
 			stepTo.setText(prevLocation);					//"To" is attribute alight_stop
 			
 			if(temp.equals("CTA") && type.equals("1"))	//Route is a CTA train.
 			{
 				temp = rtid;	//Step is now the route ID corresponding to which train
 
 				String temp2 = attr.item(3).getNodeValue();
 				temp2.replace(' ', '\n');
 				stepTag.setText(temp2);	//Set step label (text) to long id name (eg. "Green Line")
 			}
 			else 
 			{
 				stepTag.setText(rtid);		//Set step text to id name (eg. bus number)
 			}
 			
 			Routes.setStepImage(stepImage, temp);		//Set image corresponding to step
 		}
 		else //walk node
 		{	
 			Routes.setStepImage(stepImage, nodeName);		//Set image corresponding to step
 			NodeList s = curr.getChildNodes();
 			double num = 0;		//Used to add lengths of steps (will be converted to miles/feet)
 			int timeDif = 0;	//Used to determine total time of route
 			String temp = "0";
 			
 			for(int y=0; y<s.getLength(); ++y)
 			{
 				NamedNodeMap attr1 = s.item(y).getAttributes();
 				Log.i("Detail", s.item(y).getNodeName());
 				
 				if(s.item(y).getNodeName().equals("start"))
 				{
 					Log.i("startNode", "inside start node");
 					temp = attr1.item(0).getNodeValue();		//Value added to total length
					timeDif = (Integer.parseInt(attr1.item(3).getNodeValue()) - 
					Integer.parseInt(attr1.item(2).getNodeValue()));
 					
 					stepFrom.setText(RouteDetail.ORIGIN);
 					
 				}
 				else if(s.item(y).getNodeName().equals("end"))
 				{
 					
 					temp = attr1.item(0).getNodeValue();	//Value added to total length
					timeDif = (Integer.parseInt(attr1.item(3).getNodeValue()) - 
					Integer.parseInt(attr1.item(2).getNodeValue()));
 					
 					stepTo.setText(RouteDetail.DESTINATION);
 				}
 				else
 				{
 					temp = attr1.item(1).getNodeValue();	//Value added to total length
 					Log.i("StreetNode", attr1.item(3).getNodeValue() + " , " + attr1.item(3).getNodeName() );
					timeDif = (Integer.parseInt(attr1.item(4).getNodeValue()) - 
 							Integer.parseInt(attr1.item(3).getNodeValue()));
 					
 					if(y == 0)
 					{
 						stepFrom.setText(attr1.item(0).getNodeValue());
 					}
 					
 					stepTo.setText(attr1.item(0).getNodeValue());
 				}
 				
				if( timeDif > 60 )
					{stepFor.setText(Integer.toString(timeDif / 60) + " min");}
				else{ stepFor.setText(Integer.toString(timeDif) + " sec");}
 				
 				num += Integer.valueOf(temp);
 			} 
			
 			String from = (String) stepFrom.getText();
 			if(from.equalsIgnoreCase("Unknown") || from.length() <= 0)
 			{
 				stepFrom.setText(prevLocation);
 			}
 			
 			//num now holds length of walk in meters.
 			//convert to miles (or feet)...
 			stepTag.setText(Routes.convertFromMeters(num));
 			
 		}//End "else" (walk node)
         //END SET IMAGE AND TAG  -------------------------------------------------------------------
         
         
         
         TextView pos = (TextView) convertView.findViewById(R.id.row_pos);
         pos.setText(String.valueOf(position));
         
         convertView.setOnClickListener(this);
         return convertView;
     }
 
     public void onClick(View view) 
     {
     	
     	//TODO: Go to map for this step.
 
     	//stepList.
     	TextView pos = (TextView) view.findViewById(R.id.row_pos);
         
     	int p = Integer.valueOf((String) pos.getText());
     	RouteDetail a  = (RouteDetail) parentActivity;
     	a.showMap(p);
     }
 }

 
 
 public class DUBwiseProps
 
 {
 
 
     public String[] optional_feature_strings={"J2MEMaps","OpenLAPI"};
     
     
     //    public String[] sound_strings={"en_speedy","en_wav","de_tts","de_wav","de_64kbit_tts","wav","no_voice"};
     
 
 
     // must be sorted - width up
     public int[][] res_vals = {{128,128},{176,220},{200,300},{240,320},{340,400}, {480,640} };
     public String[] res_strings;
     
     public String[] feature_strings={"Bluetooth","Location API","File Connection","Device Control","cldc11"};
     
     
     public String[] sound_strings={"no sound","en mp3@32kbit","en mp3@64kbit","en wav"};
     public String[] sound_clean_strings={"no_voice","en_mp3_32kbit","en_mp3_64kbit","en_wav"};
     
     public String[] firmware_strings={"No Firmwars","All Firmwares","FC&MK3MAG Firmwares"};
     public String[] firmware_clean_strings={"no_firmwares","all_firmwares","fc_mk3mag_firmwares"};
 
 
     public String[] installsrc_strings={"stable (latest tag)","Bleeding Edge (trunk)"};
     public String[] installsrc_clean_strings={"tags","trunk"};
 
 
 
     // selecables
 
     public int res_select=0;
     public int sound_select=0;
     public int firmware_select=0;
     public int installsrc_select=0;
 
 
     public boolean cldc11=false;
     public boolean bluetooth=false;
     public boolean fileapi=false;
     public boolean devicecontrol=false;
 
     public boolean jsr179=false;
     public boolean sensor_api=false;
 
 
     public boolean j2memaps=false;
     public boolean openlapi=false;
 
 
     public DUBwiseProps()
     {
 	try
 	    {
 		res_strings=new String[res_vals.length];
 		for (int i=0;i<res_vals.length;i++)
 			res_strings[i]=res_vals[i][0]+"x"+res_vals[i][1];
 
 	    }
 	catch(Exception e)
 	    {
 		res_strings=new String[0];
 	    }
 
     }
 
     public String get_code()
     {
 	byte bval0=0,bval1=0,bval2=0;
 
 	if (bluetooth) bval0|=1;
 	if (cldc11) bval0|=2;
 	if (devicecontrol) bval0|=4;
 
 	if (fileapi) bval1|=1;
 	if (jsr179) bval1|=2;
 	if (sensor_api) bval1|=4;
 
 	if (j2memaps) bval2|=1;
 	if (openlapi) bval2|=2;
 
 
	return ""+bval0+""+bval1+""+res_select+""+sound_select+""+firmware_select;
     }
     public boolean set_code(String code)
     {
 
 	if (code.length()<5)
 	    return false;
 	for (int i=0 ; i<code.length();i++)
 	    {
 		int val=code.charAt(i)-'0';
 		switch(i)
 		    {
 		    case 0:
 			
 			bluetooth=((val&1)!=0);
 			cldc11=((val&2)!=0);
 			devicecontrol=((val&4)!=0);
 
 			break;
 
 
 		    case 1:
 			
 			fileapi=((val&1)!=0);
 			jsr179=((val&2)!=0);
 			sensor_api=((val&4)!=0);
 			break;
 		    case 2:
 			res_select=val;
 			break;
 			
 		    case 3:
 			sound_select=val;
 			break;
 			
 		    case 4:
 			firmware_select=val;
 			break;
 
 		    case 5:
 			j2memaps=((val&1)!=0);
 			openlapi=((val&2)!=0);
 			break;
 			
 		    default:
 			return false;
 		    }
 		
 	    }
 	try
 	    {
 		_getFileName();
 		return true;
 	    }
 	catch(Exception e)
 	    {
 		return false;
 	    }
 
 
     }
 
     // setter
 
     public void set_res_by_screensize(int width,int height)
     {
 
 	for (int i=0;i<res_vals.length;i++)
 	    if (width>=res_vals[i][0])
 		res_select=i;
     }
 
     // getter
 
 
     public String cldc_str()
     {
 	if (cldc11)
 	     return "-CLDC11";
 	else
 	     return "";
     }
 
     public String bt_str()
     {
 	if (bluetooth)
 	     return "-BluetoothAPI";
 	else
 	     return "";
     }
 
     public String fileapi_str()
     {
 	if (fileapi)
 	     return "-FileAPI";
 	else
 	     return "";
     }
 
 
     public String firmware_str()
     {
 	return firmware_clean_strings[firmware_select];
     }
 
     public String devicecontrol_str()
     {
 	if (devicecontrol)
 	     return "-DeviceControl";
 	else
 	     return "";
     }
 
 
     public String map_str()
     {
 	if (j2memaps)
 	     return "-J2MEMap";
 	else
 	     return "";
     }
 
     public String sound_str()
     {
 	return sound_clean_strings[sound_select];
     }
 
 
 
     public String _getFileName()
     {
 	return "DUBwise-"+res_str()+"-" + sound_str() + "-"  + firmware_str() + cldc_str() + fileapi_str() + bt_str() + devicecontrol_str() + map_str() ;
     }
 
 
     public String getJADFileName()
     {
 	return _getFileName()+".jad";
     }
 
     public String getJARFileName()
     {
 	return _getFileName()+".jar";
     }
 
 
 
     
 
     public String res_str()
     {
 	return  res_strings[res_select];
     }
 
     
 
     public String installsrc_str()
     {
 	return installsrc_clean_strings[installsrc_select];
     }
 
 
 
 }

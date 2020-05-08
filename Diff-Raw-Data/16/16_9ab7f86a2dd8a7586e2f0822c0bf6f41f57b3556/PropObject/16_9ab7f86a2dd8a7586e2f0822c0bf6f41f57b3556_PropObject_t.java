 package org.concord.waba.extra.util;
 import waba.ui.Control;
 import org.concord.waba.extra.io.*;
 
 public class PropObject
 {
 	public static int EDIT = 0;
 	public static int CHOICE = 1;
 	public static int CHOICE_SETTINGS = 2;
 	public static int MULTIPLE_SEL_LIST = 3;
 	public static int CHECK = 4;
 
 	String 	label;
 	String  name;
 	int id;
 
 	// Actual Values
 	String 	[]possibleValues;
 	boolean []checkedValues;
 	String 	value;
 	int index = -1;
 	boolean checked = false;
 
 	// Visible values the user can see
 	String []visPossibleValues;
 	boolean []visCheckedValues;
 	String visValue;
 	int visIndex = -1;
 	boolean visChecked = false;
 
 	Object	cookie = null;
 	public int prefWidth = 60;
 	String settingsButtonName;
 
 	int type = EDIT;
 	boolean radio = false;
 
 	public PropObject(DataStream in){
 		cookie = null;
 		readExternal(in);
 	}
 
 	public 	PropObject(String label, String name, int id, String []possibleValues,int defaultIndex){
 		this.label = label;
 		this.id = id;
 		this.name = name;		
 		this.possibleValues = possibleValues;
 		visPossibleValues = possibleValues;
 		if(possibleValues != null && possibleValues.length > 0){
 			type = CHOICE;
 			value = possibleValues[defaultIndex];
 			index = defaultIndex;
 			visValue = new String(value);
 			visIndex = index;
 		} 
 	}
 	public 	PropObject(String label, String name, int id, String []possibleValues){
 		this(label, name, id, possibleValues,0);
 	}
 	public 	PropObject(String label, String name, int id,String value){
 		this(label,name,id,null,0);
 		setValue(value);
 	}
 	public 	PropObject(String label, String name, int id){
 		this(label, name, id, null,0);
 	}
 	public PropObject(String label, String name, int id, boolean flag){
 		this(name,null,0);
 		type = CHECK;
 		visChecked = checked = flag;
 	}
 
 	public void setType(int t){
 		type = t;
 		if(type == MULTIPLE_SEL_LIST){
 			checkedValues = new boolean [possibleValues.length];
 			visCheckedValues = new boolean [possibleValues.length];
 		}
 	}
 	public int getType(){return type;}
 
 	public void setRadio(boolean r){radio = r;}
 	public boolean getRadio(){return radio;}
 
 	public String getSettingsButtonName(){return settingsButtonName;}
 	public void setSettingsButtonName(String sbn){settingsButtonName = sbn;}
 
 	public void setPossibleValues(String [] values)
 	{
 		possibleValues = values;
 		visPossibleValues = values;
 		checkedValues = new boolean [values.length];
 		visCheckedValues = new boolean [values.length];
 		index = 0;
 		visIndex = index;
 	}
 
 	public void setVisPossibleValues(String [] values)
 	{
 		visPossibleValues = values;
 		visCheckedValues = new boolean [values.length];
		setVisIndex(0);
 	}
 
 	public void setCheckedValue(int index, boolean on)
 	{
 		if(checkedValues != null &&
 		   index >= 0 && index < checkedValues.length){
 			checkedValues[index] = on;
 			visCheckedValues[index] = on;
 		}
 	}
 
 	public void setVisCheckedValue(int index, boolean on)
 	{
 		if(visCheckedValues != null &&
 		   index >= 0 && index < checkedValues.length){
 			visCheckedValues[index] = on;
 		}
 	}
 
 	public boolean setVisCheckedValues(boolean values[])
 	{
 		for(int i=0; i<visCheckedValues.length; i++){
 			visCheckedValues[i] = values[i];
 		}
 		return true;
 	}
 
 	public boolean getCheckedValue(int index)
 	{
 		if(checkedValues != null &&
 		   index >= 0 && index < checkedValues.length){
 			return checkedValues[index];
 		}
 		return false;
 	}
 
 	public boolean getVisCheckedValue(int index)
 	{
 		if(checkedValues != null &&
 		   index >= 0 && index < checkedValues.length){
 			return visCheckedValues[index];
 		}
 		return false;
 	}
 
 	public void setChecked(boolean on)
 	{
 		checked = on;
 		visChecked = on;
 	}
 
 	public boolean setVisChecked(boolean on)
 	{
 		visChecked = on;
 		return true;
 	}
 
 	public boolean getChecked(){ return checked; }
 	public boolean getVisChecked(){ return visChecked; }
 	
 	public void setCookie(Object c){
 		cookie = c;
 	}
 	public Object getCookie(){return cookie;}
 
 	public void apply()
 	{
		// set the visible value to the actual one
 		if(value != null) value = new String(visValue);
 		if(type == CHOICE || type == CHOICE_SETTINGS){
 			index = visIndex;
 			possibleValues = visPossibleValues;
 		} else if(type == MULTIPLE_SEL_LIST){
 			if(visCheckedValues != null && checkedValues != null){
 				if(checkedValues.length != visCheckedValues.length){
 					checkedValues = new boolean [visCheckedValues.length];
 				}
 				for(int i=0; i < checkedValues.length; i++){
 					checkedValues[i] = visCheckedValues[i];
 				}
 				possibleValues = visPossibleValues;
 			}
 		} else if(type == CHECK){
 			checked = visChecked;
 		}
 	}
 
 	public void close()
 	{
 		if(value != null && !value.equals(visValue)) visValue = new String(value);
 		if(type == CHOICE || type == CHOICE_SETTINGS){
 			visIndex = index;
 			visPossibleValues = possibleValues;
 		} else if(type == MULTIPLE_SEL_LIST){
 			if(checkedValues != null){
 				if(checkedValues.length != visCheckedValues.length){
 					visCheckedValues = new boolean [checkedValues.length];
 				}
 				for(int i=0; i < checkedValues.length; i++){
 					checkedValues[i] = visCheckedValues[i];
 				}
 				possibleValues = visPossibleValues;				
 			}
 		} else if(type == CHECK){
 			visChecked = checked;
 		}
 	}
 
 	public void setIndex(int i)
 	{
 		if((type == CHOICE || type == CHOICE_SETTINGS) &&
 		   i >= 0 && i < possibleValues.length){
 			index = i;
 			visIndex = index;
 		}
 	}
 
 	public void setValue(String value){
 		if(value == null) value = "";
 		boolean inPossibleValue = false;
 		if(possibleValues == null){
 			inPossibleValue = true;
 		}else{
 			for(int i = 0;i < possibleValues.length; i++){
 				if(value.equals(possibleValues[i])){
 					inPossibleValue = true;
 					index = i;
 					visIndex = index;
 					break;
 				}
 			}
 		}
 		if(inPossibleValue){
 			this.value = value;
 			visValue = new String(value);
 		}
 	}
 	
	public void setVisIndex(int i)
	{
		if((type == CHOICE || type == CHOICE_SETTINGS) &&
		   i >= 0 && visPossibleValues != null &&
		   i < visPossibleValues.length){
			visIndex = i;
			visValue = visPossibleValues[i];
		}
	}

 	/*
 	 * return:
 	 * -1 means invalid value
 	 * 0 means no change
 	 * 1 means new value;
 	 */
 	public int setVisValue(String value){
 		int newIndex = -1;
 		if(value == null) return -1;
 		boolean inPossibleValue = false;
 		if(visPossibleValues == null){
 			inPossibleValue = true;
 		}else{
 			for(int i = 0;i < visPossibleValues.length; i++){
 				if(value.equals(visPossibleValues[i])){
 					inPossibleValue = true;
 					newIndex = i;
 					break;
 				}
 			}
 		}
 		if(inPossibleValue){
 			if(!value.equals(visValue)){
 				visValue = value;
 				visIndex = newIndex;
 				return 1;
 			} else {
 				return 0;
 			}
 		}
 		return -1;
 	}
 
 	private float getFValue(String value){
 		if(value != null){
 			return ConvertExtra.toFloat(value);
 		}
 		return Maths.NaN;
 	}
 	public float getFValue(){ return getFValue(value);}
 	public String getValue(){ return value;}
 	public int getIndex(){return index;}
 
 	public float getVisFValue(){ return getFValue(visValue);}
 	public String getVisValue(){return visValue;}
 	public int getVisIndex(){return visIndex;}
 
 	public String [] getPossibleValues(){ return possibleValues;}
 	public String [] getVisPossibleValues(){ return visPossibleValues;}
 
 	public String getName(){ return name;}
 	public String getLabel(){ return label;}
 	public int getId(){ return id;}
 	
 	public void writeExternal(DataStream out){
 		out.writeInt(id);
 		out.writeInt(type);
 		out.writeBoolean(value != null);
 		if(value != null) out.writeString(value);
 		out.writeFloat(getFValue(value));
 		out.writeBoolean(possibleValues != null);
 		if(possibleValues != null){
 			out.writeInt(possibleValues.length);
 			for(int i = 0; i < possibleValues.length; i++){
 				out.writeBoolean(possibleValues[i] != null);
 				if(possibleValues[i]  != null) out.writeString(possibleValues[i] );
 			}
 		}
 	}
 
 	public static int readExternalId(DataStream in)
 	{
 		return in.readInt();
 	}
 	public void readExternalUpdate(DataStream in)
 	{
 		type = in.readInt();
 		value = (in.readBoolean())?in.readString():null;
 		if(value != null) visValue = new String(value);
 		float fval = in.readFloat();
 		possibleValues = null;
 		if(in.readBoolean()){
 			int n = in.readInt();
 			if(n > 0){
 				possibleValues = new String[n];
 				for(int i = 0; i < n; i++){
 					possibleValues[i] = (in.readBoolean())?in.readString():null;
 				}
 			}
 			for(int i = 0;i < possibleValues.length; i++){
 				if(value.equals(possibleValues[i])){
 					index = i;
 					visIndex = i;
 					break;
 				}
 			}
 			visPossibleValues = possibleValues;
 		}
 		if(possibleValues != null) type = CHOICE;
 	}
 
 	public void readExternal(DataStream in){
 		id = in.readInt();
 		readExternalUpdate(in);
 	}
 }

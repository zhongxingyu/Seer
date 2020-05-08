 package org.xfon.m.coc;
 
 import java.util.LinkedHashMap;
 
 import org.xfon.m.coc.gui.CustomNumberPicker;
 import org.xfon.m.coc.gui.CustomNumberPicker.OnChangedListener;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.widget.TextView;
 
 public class Investigator  implements OnChangedListener {
 	private Activity activity;
 	
 	private String name;
 	private Age age;
 	
 	private Dice dice_3d6 = new Dice( 3,6 );
 	private Dice dice_2d6p6 = new Dice( 2,6,6 );
 	private Dice dice_3d6p3 = new Dice( 3,6,3 );
 	
 	private StrengthDamageBonus strDamBonus = new StrengthDamageBonus();
 	
 	private Attribute attrStr, attrCon, attrSiz, attrDex;
 	private Attribute attrApp, attrInt, attrPow, attrEdu;
 	private Attribute[] baseAttributes, ageModifiableAttributes;
 	
 	private LinkedHashMap<String,Attribute> baseAttributesMap;
 	private int mustDrop;	
 	
 	public Investigator( Activity activity, Age age ) {
 		this.activity = activity;
 		this.age = age;
 		this.name = "-";
 		
 		mustDrop = -1;
 		
 		attrStr = new Attribute( activity, "STR", R.id.tv_str, dice_3d6 );
 		attrCon = new Attribute( activity, "CON", R.id.tv_con, dice_3d6 );
 		attrSiz = new Attribute( activity, "SIZ", R.id.tv_siz, dice_2d6p6 );
 		attrDex = new Attribute( activity, "DEX", R.id.tv_dex, dice_3d6 );
 		
 		attrApp = new Attribute( activity, "APP", R.id.tv_app, dice_3d6 );
 		attrInt = new Attribute( activity, "INT", R.id.tv_int, dice_2d6p6 );
 		attrPow = new Attribute( activity, "POW", R.id.tv_pow, dice_3d6 );
 		attrEdu = new Attribute( activity, "EDU", R.id.tv_edu, dice_3d6p3 );
 				
 		ageModifiableAttributes = new Attribute[] {
 				attrStr, attrCon, attrDex, attrApp
 		};
 		
 		baseAttributes = new Attribute[] {
 				attrStr, attrCon, attrSiz, attrDex,
 				attrApp, attrInt, attrPow, attrEdu
 		};
 
 		baseAttributesMap = new LinkedHashMap<String,Attribute>();
 		for ( int i = 0; i < baseAttributes.length; i++ ) {
 			baseAttributesMap.put( baseAttributes[ i ].getName(), baseAttributes[ i ] );
 		}
 	}
 		
 	public Attribute[] getBaseAttributes() {
 		return baseAttributes;
 	}
 	
 	public Attribute[] getAgeModifiableAttribute() {
 		return ageModifiableAttributes;
 	}
 	
 	public Attribute getAttribute( String name ) {
 		return baseAttributesMap.get( name );
 	}
 	
 	public void rerollBasicAttributes() {
 		for ( int i = 0; i < baseAttributes.length; i++ ) {
     		baseAttributes[i].roll(); // this also clears all mods
     	}    			
 		setBaseAge();		
 		mustDrop = 0;
 	}
 	
 	public void setBaseAge() {
		age.setBaseAge( attrEdu.getTotal() + 6 );
 	}
 	
 	public int getAge() {
 		return age.getAge();
 	}
 	
 	public void setAge( int newAge ) {
 		age.setAge( newAge );
 	}	
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getTotalAgeMods() {
 		int sum = 0;
     	for ( int i = 0; i < ageModifiableAttributes.length; i++ ) sum += ageModifiableAttributes[ i ].getMod();
     	return sum;
 	}
 	
 	public String getDamBonus() {
 		return strDamBonus.getBonus( attrStr.getTotal(), attrSiz.getTotal() );
 	}
 
 	@Override
 	public void onChanged(CustomNumberPicker picker, int oldVal, int newVal) {
 		if ( !age.isAgeNumberPicker(picker) )  return;
 		int selectedAge = newVal;
 		int ageDiff = selectedAge - age.getBaseAge();
 		int extraEdu = ageDiff / 10;
 		attrEdu.setMod( extraEdu );
 		// calculateDerivedAttributes(); // TODO 
 		
 		int newMustDrop = Math.max( 0, ( selectedAge / 10 ) - 3 );
 		if ( newMustDrop != mustDrop ) {
 			mustDrop = newMustDrop;
 			//updateMustDrop();
 		}						
 	}
 	
 }

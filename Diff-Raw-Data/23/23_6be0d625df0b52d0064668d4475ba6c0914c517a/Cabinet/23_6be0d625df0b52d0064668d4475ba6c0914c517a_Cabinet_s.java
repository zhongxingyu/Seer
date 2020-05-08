 /**
  * @author Mateusz Szygenda
  *
  */
 package line6.commands.values;
 
 import line6.Misc;
 import line6.commands.parameters.BaseParameter;
 import line6.commands.parameters.Parameter;
 
 public enum Cabinet implements BaseParameter {
	SmallTweed2_1x8(0, "1960 Fender Tweed Champ"),
	SmallTweed_1x12(1, "1952 Fender Tweed Deluxe Reverb"),
	BritishClassA3_1x12(2,"1960 Vox AC-15"),
	BlackPanel_1x12(3, "1964 Fender Blackface Deluxe Reverb"),
 	Line6Flextone98_1x12(4),
	BlackPanel2_2x12(5,"1965 Fender Blackface Twin Reverb"),
	BritishClassA_2x12(6,"1967 Vox AC-30"),
 	ModernClassA_2x12(7,"1995 Matchless Chieftain"),
 	Line6Custom98_2x12(8),
	TweedBlues_4x10(9,"1959 Fender Bassman"),
 	Line6Custom_4x10(10),
	BritishHighGain_4x12(11,"1996 Marshall with Vintage 30s"),
	BritishHighGain2_4x12(12,"1978 Marshall with stock 70s"),
	BritishHighGain3_4x12(13,"1968 Marshall Basketweave with Greenbacks"),
 	Line6Custom98_4x12(14),
 	NoCabinetEmulation(15);
 	
 	private int cabinet_id;
 	private String realCabinetName;
 	
 	private Cabinet(int id)
 	{
 		this(id, new String());
 	}
 	
 	private Cabinet(int id, String realName)
 	{
 		cabinet_id = id;
 		realCabinetName = realName;
 	}
 
 	@Override
 	public int id() {
 		return cabinet_id;
 	}
 
 	@Override
 	public int getMaxValue() {
 		return id();
 	}
 	
 	public static BaseParameter getValue(int valueId)
 	{
 		 for(Cabinet c : line6.commands.values.Cabinet.values())
 		 {
 			 if(c.id() == valueId)
 				 return c;
 		 }
 		 return Parameter.Unknown;
 	}
 
 	@Override
 	public int getMinValue() {
 		return 0;
 	}
 	
 	public String toString()
 	{
 		if(!realCabinetName.isEmpty())
 			return String.format("%s - based on %s", Misc.splitByUppercase(super.toString()), realCabinetName);
 		else
 			return Misc.splitByUppercase(super.toString());
 	}
 }

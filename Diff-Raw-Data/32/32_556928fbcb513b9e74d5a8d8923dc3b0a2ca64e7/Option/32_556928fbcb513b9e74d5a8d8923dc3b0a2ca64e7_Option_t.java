 package pikater.ontology.messages;
 
 import pikater.gui.java.MyWekaOption.dataType;
 import jade.content.Concept;
 import jade.util.leap.List;
 
 public class Option implements Concept {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6943758563650887842L;
 	private boolean _mutable;
 	private Interval _range;
 	private List _set;
 	private boolean _is_a_set;
 	private Interval _number_of_args;
 	private String _data_type;
 	private String _description;
 	private String _name;
 	private String _synopsis;
 	private String _value; // when immutable, contains the default value
 	private String _default_value;
 	private String _user_value;
 	private int _number_of_values_to_try;
         private int numberOfOptions;
 
     public int getNumberOfOptions() {
         return numberOfOptions;
     }
 
     public void setNumberOfOptions(int numberOfOptions) {
         this.numberOfOptions = numberOfOptions;
     }
 
 
 
 	// Methods required to use this class to represent the TASK role
 	public void setMutable(boolean mutable) {
 		_mutable = mutable;
 	}
 
 	public boolean getMutable() {
 		return _mutable;
 	}
 
 	public void setRange(Interval range) {
 		_range = range;
 	}
 
 	public Interval getRange() {
 		return _range;
 	}
 
 	public void setSet(List set) {
 		_set = set;
 	}
 
 	public List getSet() {
 		return _set;
 	}
 
 	public void setIs_a_set(boolean is_a_set) {
 		_is_a_set = is_a_set;
 	}
 
 	public boolean getIs_a_set() {
 		return _is_a_set;
 	}
 
 	public void setNumber_of_args(Interval number_of_args) {
 		_number_of_args = number_of_args;
 	}
 
 	public Interval getNumber_of_args() {
 		return _number_of_args;
 	}
 
 	public void setData_type(String data_type) {
 		_data_type = data_type;
 	}
 
 	public String getData_type() {
 		return _data_type;
 	}
 
 	public void setDescription(String description) {
 		_description = description;
 	}
 
 	public String getDescription() {
 		return _description;
 	}
 
 	public void setName(String name) {
 		_name = name;
 	}
 
 	public String getName() {
 		return _name;
 	}
 
 	public void setSynopsis(String synopsis) {
 		_synopsis = synopsis;
 	}
 
 	public String getSynopsis() {
 		return _synopsis;
 	}
 
 	public void setValue(String value) {
 		_value = value;
 	}
 
 	public String getValue() {
 		return _value;
 	}
 
 	public void setDefault_value(String default_value) {
 		_default_value = default_value;
 	}
 
 	public String getDefault_value() {
 		return _default_value;
 	}
 
 	public void setUser_value(String user_value) {
 		_user_value = user_value;
 	}
 
 	public String getUser_value() {
 		return _user_value;
 	}
 
 	public int getNumber_of_values_to_try() {
 		return _number_of_values_to_try;
 	}
 
 	public void setNumber_of_values_to_try(int number_of_values_to_try) {
 		_number_of_values_to_try = number_of_values_to_try;
 	}
 
 	public Option(){		
 	
 	}
 	
 	public Option(String name, String data_type,
 			float numArgsMin, float numArgsMax,
 			String range, float rangeMin, float rangeMax, List set,
 			String default_value, String synopsis, String description){		
 
 		_mutable = false;
 		
 		_name = name;
 		_data_type = data_type;
 		
		if (!data_type.equals("BOOLEAN")){
			_number_of_args = new Interval();
			_number_of_args.setMin((float)numArgsMin);
			_number_of_args.setMax((float)numArgsMax);
				
			if (range.equals("r")) {
				_range = new Interval();
				_range.setMin(rangeMin);
				_range.setMax(rangeMax);
				_is_a_set = false;
			}
			if (range.equals("s")) {
				_is_a_set = true;
				_set = set; 
			}
		}		
 		_description = description;
 		_synopsis  = synopsis;
 		
 		_value = default_value;
 		_default_value = default_value;
 	}
 	
 	public Option copyOption(){
 		Option opt = new Option();
 		opt.setMutable(_mutable);
 		opt.setRange(_range);
 		opt.setSet(_set);
 		opt.setIs_a_set(_is_a_set);
 		opt.setNumber_of_args(_number_of_args);
 		opt.setData_type(_data_type);
 		opt.setDescription(_description);
 		opt.setName(_name);
 		opt.setSynopsis(_synopsis);
 		// opt.setValue(_value.substring(0)); // when immutable, contains the default value
 		opt.setValue(_value); // when immutable, contains the default value
 		opt.setDefault_value(_default_value);
 		if(_user_value!=null)
 			opt.setUser_value(_user_value);
 		opt.setNumber_of_values_to_try(_number_of_values_to_try);
 	    opt.setNumberOfOptions(numberOfOptions);
 	    return opt;
 	}
 }

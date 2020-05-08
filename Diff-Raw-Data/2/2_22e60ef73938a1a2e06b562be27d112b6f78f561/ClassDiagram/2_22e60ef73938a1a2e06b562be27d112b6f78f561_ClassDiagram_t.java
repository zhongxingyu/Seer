 /**
  * @opt hide
 */
 class string{}
 /**
  * @opt hide
 */
 class HashMap{}
 /**
  * @opt hide
 */
 class ArrayList<Monster>{}
 
 /**
  * @composed 1 - * Monster
 */
 class UserAccount extends java.util.Observable {
 	private long primaryKey;
 	private string email;
 	
 	private string password;
 	private ArrayList<Monster> listOfMonsters;
 		
 	public UserAccount(long primaryKey){}
 	
 	public boolean checkPassword(string password){}
 	public UserAccount setPassword(String password){}
 	
 	public UserAccount setEmail(String email){}
 	public String getEmail(){}
 	
 	public JSONObject buildJSON(){}
 	public UserAccount readJSON(){}
 }
 /**
  * @composed 1 - * UserAccount
 */
 class TableOfAccounts implements java.util.Observer {
 	private long nextAccountKey;
 	private HashMap accounts;
 
 	public TableOfAccounts(){}
 		
 	public UserAccount lookup(string email){}
 	public UserAccount addUser(string email){}
 	
 	public JSONObject buildJSON(){}
 	public TableOfAccounts readJSON(String expression){}
 }
 
 /**
   * @note 'Amplitude' is the value of an attribute at its peak. A 'Lambda', or time-coefficient,
   governs the rate of change in an exponential function - it is called lambda by analogy to 
   physical processes with exponential functions (ie Radioactive Decay). The former is assigned on 
   a per-attribute basis (i.e. a seperate gene for each attribute's Amplitude). The latter appears 
   to be global for all attributes - using a single 'age_rate' double. Also note that 'Amplitudes' 
   are integer values, while 'Lambdas' are rational (a smaller fraction indicates a slower aging 
   process - greater than 1 and the creature has the life-expectancy of a mayfly).
 
   * @note getHealth returns the Monster's health at the instant it is called. A creature whose 
   instantaneous health is greater than 0 is 'alive' and may perform actions. A creature whose 
   instantaneous health is less than or equal to 0 has died of natural causes. A creature's health 
   at any instant is given by 2-e^(\lambda t). \lambda is the coefficient of decay, a smaller 
   (fractional) value indicates that the creature will age more slowly. t is the time in days. 
  The natural lifespan of a creature should be given by t = ln(2)/\lambda. A creature's Health can be 
   reduced by Injuries - bringing about an earlier demise.
 
   @note Strength, Evasion and Toughness are calculated from an 'Amplitude' coefficient (determining 
   their peak value) and a 'Lambda' time coefficient determining the time it takes to reach that peak, 
   and how fast the creature 'decays' from that peak. The amplitude is called 'a' and the time coefficient 
   is called 'lambda'. These three attributes are given by a[e^(\lambda t)-1][2-e^(\lambda t)]. Noting that 
   \lambda is the age_rate field and a is the 'Amplitude' for that characteristic.
 
   @note Appendix A of the Requirements Specification includes the functions dealing with reproduction and 
   fertility - they are not reproduced here for space concerns.
  */
 class Monster {
 	private long primaryKey;
 	private UserAccount owner;
 	
 	private string monsterName;
 	private boolean gender;
 	private java.util.Date dateOfBirth;
 
 	protected double age_rate;
 	
 	protected int strength_amplitude;
 
 	protected int evade_amplitude;
 
 	protected int fertility;
 
 	protected double injuryChance;
 	protected int injuries;
 
 	protected int toughness_amplitude;
 
 	public double getHealth();
 	public int getStrength();
 	public int getEvade();
 	public double getFertility();
 	public double getInjuryChance();
 	public void injure();
 	public void kill();
 	public int getMaxHP();
 
 
 }
 	
 
 	
 

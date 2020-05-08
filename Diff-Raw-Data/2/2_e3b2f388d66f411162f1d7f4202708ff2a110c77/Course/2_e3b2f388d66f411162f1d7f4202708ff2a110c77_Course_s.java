 package util.webpage;
 
 import java.util.ArrayList;
 
 import util.BitOperate;
 import util.BitOperate.BitOperateException;
 import util.webpage.Course.TimeAndAddress.TimeAndAddressException;
 
 /**
  * <strong>Note:</strong>大部分用于设置的方法返回this，做Builder
  * @author Zhou Peican
  * @improver Bai Jie
  */
 public class Course {
 	/**本地数据库使用的ID*/
 	private int id;
 	/**课程代码*/
 	private String code;
 	/**课程名称*/
 	private String name;
 	/**课程教师<br />暂没教师详情*/
 	private ArrayList<String> teachers;
 	/**学分*/
 	private byte credit;
 	/**教学班号*/
 	private String classNumber;
 	
 	private ArrayList<TimeAndAddress> timeAndAddress;
 	//暂没参考教材
 	private String teachingMaterial;
 	/**备注*/
 	private String note;
 	/**学年*/
 	private short year;
 	/**true表示上半学期，false表示下半学期，null表示未知*/
 	private Boolean isFirstSemester;
 	/**结课考核成绩*/
 	private short testScore;
 	/**期末总评成绩*/
 	private short totalScore;
	/**期末总评成绩*/
 	private String kind;
 	
 
 	/**
 	 * 无参构造方法，各属性设为默认空值
 	 */
 	public Course(){
 		id = -1;
 		code = name = classNumber = teachingMaterial = note = kind= null;
 		credit = 0;
 		year = 0;
 		isFirstSemester = null;
 		testScore = totalScore = -1;
 		teachers = new ArrayList<String>();
 		timeAndAddress = new ArrayList<TimeAndAddress>();
 	}
 	public Course(String code, String name, int credit, String classNumber) throws CourseException{
 		this();
 		this.code = code;
 		this.name = name;
 		setCredit(credit);
 		this.classNumber = classNumber;
 	}
 	public Course(String code, String name, int credit, String classNumber, int testScore, 
 			int totalScore, int year, Boolean isFirstSemester) throws CourseException{
 		this(code, name, credit, classNumber);
 		setTestScore(testScore);
 		setTotalScore(totalScore);
 		setYear(year);
 		this.isFirstSemester = isFirstSemester;
 	}
 	/**全参构造方法
 	 * @throws CourseException */
 	public Course(int id, String code, String name, String[] teachers, int credit, String classNumber, 
 			TimeAndAddress[] timeAndAddresses, String teachingMaterial, String note, int year,
 			Boolean isFirstSemester, int testScore, int totalScore, String kind) throws CourseException{
 		this(code, name, credit, classNumber, testScore, totalScore, year, isFirstSemester);
 		this.id = id;
 		this.teachingMaterial = teachingMaterial;
 		//对teachers数组初始化
 		if(teachers!=null && teachers.length>0)
 			for(String teacher:teachers)
 				if(teacher != null)
 					this.teachers.add(teacher);
 		if(timeAndAddresses!=null && timeAndAddresses.length>0)
 			for(TimeAndAddress TA:timeAndAddresses)
 				if(TA != null)
 					timeAndAddress.add(new TimeAndAddress(TA));
 		this.note = note;
 		this.kind = kind;
 	}
 	/**拷贝构造方法*/
 	public Course(Course src) throws CourseException{
 		this(src.code, src.name, src.credit, src.classNumber, src.testScore, src.totalScore, src.year, src.isFirstSemester);
 		this.id = src.id;
 		this.teachingMaterial = src.teachingMaterial;
 		this.note = src.note;
 		setTeachers(src.teachers);
 		setTimeAndAddresse(src.timeAndAddress);
 		this.kind = src.kind;
 	}
 	
 	
 	/**
 	 * @return the id
 	 */
 	public int getId() {
 		return id;
 	}
 	/**
 	 * @param id the id to set
 	 */
 	public Course setId(int id) {
 		this.id = id;
 		return this;
 	}
 	/**
 	 * @return the code
 	 */
 	public String getCode() {
 		return code;
 	}
 	/**
 	 * @param code the code to set
 	 */
 	public Course setCode(String code) {
 		this.code = code;
 		return this;
 	}
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 	/**
 	 * @param name the name to set
 	 */
 	public Course setName(String name) {
 		this.name = name;
 		return this;
 	}
 	/**
 	 * 可直接用此方法的返回引用操作，对之的操作会作用在此Course对象上
 	 * @return the teachers
 	 */
 	public ArrayList<String> getTeachers() {
 		return teachers;
 	}
 	/**
 	 * 把本Course对象的teachers清空并设置为参数指定的内容
 	 * @param teachers the teachers to set
 	 * @throws NullPointerException 参数为null
 	 */
 	public Course setTeachers(ArrayList<String> teachers){
 		if(teachers == null)
 			throw new NullPointerException("teachers shouldn't have been null.");
 		this.teachers.clear();
 		this.teachers.addAll(teachers);
 		return this;
 	}
 	/**
 	 * 把本Course对象的teachers清空并设置为参数指定的内容
 	 * @param teachers the teachers to set
 	 * @throws NullPointerException 参数为null
 	 */
 	public Course setTeachers(String[] teachers){
 		if(teachers == null)
 			throw new NullPointerException("teachers shouldn't have been null.");
 		this.teachers.clear();
 		for(String teacher:teachers)
 			if(teacher != null)
 				this.teachers.add(teacher);
 		return this;
 	}
 	/**
 	 * 把本Course对象的teachers清空并设置为参数指定的内容
 	 * @param teachers 教师名，可以是多名教师，用[,，;；]分开
 	 * @throws NullPointerException 参数为null
 	 */
 	public Course setTeachers(String teachers){
 		if(teachers == null)
 			throw new NullPointerException("teachers shouldn't have been null.");
 		this.teachers.clear();
 		addTeacher(teachers);
 		return this;
 	}
 	/**
 	 * @return the credit
 	 */
 	public byte getCredit() {
 		return credit;
 	}
 	/**
 	 * @param credit the credit to set
 	 * @throws CourseException 参数不是正数,或者参数大于Byte.MAX_VALUE
 	 */
 	public Course setCredit(int credit) throws CourseException {
 		if(credit<=0)
 			throw new CourseException("Field credit should have been positive.");
 		else if(credit>Byte.MAX_VALUE)
 			throw new CourseException("Field credit shouldn't have been more than "+Byte.MAX_VALUE+".");
 		this.credit = (byte)credit;
 		return this;
 	}
 	/**
 	 * @return the classNumber
 	 */
 	public String getClassNumber() {
 		return classNumber;
 	}
 	/**
 	 * @param classNumber the classNumber to set
 	 */
 	public Course setClassNumber(String classNumber) {
 		this.classNumber = classNumber;
 		return this;
 	}
 	/**
 	 * 可直接用此方法的返回引用操作，对之的操作会作用在此Course对象上
 	 * @return the timeAndAddress
 	 */
 	public ArrayList<TimeAndAddress> getTimeAndAddress() {
 		return timeAndAddress;
 	}
 	/**
 	 * 把本Course对象的TimeAndAddresses清空并设置为参数指定的内容
 	 * @param timeAndAddress the timeAndAddress to set
 	 * @throws NullPointerException 参数是null
 	 */
 	public Course setTimeAndAddresse(ArrayList<TimeAndAddress> timeAndAddresses){
 		if(timeAndAddresses == null)
 			throw new NullPointerException("timeAndAddresses shouldn't have been null.");
 		this.timeAndAddress.clear();
 		for(TimeAndAddress TA:timeAndAddresses)
 			if(TA != null)
 				this.timeAndAddress.add(new TimeAndAddress(TA));
 		return this;
 	}
 	/**
 	 * 把本Course对象的TimeAndAddresses清空并设置为参数指定的内容
 	 * @param timeAndAddress the timeAndAddress to set
 	 * @throws NullPointerException 参数是null
 	 */
 	public Course setTimeAndAddresse(TimeAndAddress[] timeAndAddresses){
 		if(timeAndAddresses == null)
 			throw new NullPointerException("timeAndAddresses shouldn't have been null.");
 		this.timeAndAddress.clear();
 		for(TimeAndAddress TA:timeAndAddresses)
 			if(TA != null)
 				this.timeAndAddress.add(new TimeAndAddress(TA));
 		return this;
 	}
 	/**
 	 * @return the teachingMaterial
 	 */
 	public String getTeachingMaterial() {
 		return teachingMaterial;
 	}
 	/**
 	 * @param teachingMaterial the teachingMaterial to set
 	 */
 	public Course setTeachingMaterial(String teachingMaterial) {
 		this.teachingMaterial = teachingMaterial;
 		return this;
 	}
 	/**
 	 * @return the note
 	 */
 	public String getNote() {
 		return note;
 	}
 	/**
 	 * @param note the note to set
 	 */
 	public Course setNote(String note) {
 		this.note = note;
 		return this;
 	}
 	/**
 	 * @return the year
 	 */
 	public short getYear() {
 		return year;
 	}
 	/**
 	 * @param year the year to set
 	 * @throws CourseException when year<1900 || year>9999
 	 */
 	public Course setYear(int year) throws CourseException {
 		if(year<1900 || year>9999)
 			throw new CourseException("Illegal year!");
 		this.year = (short) year;
 		return this;
 	}
 	/**
 	 * @return the isFirstSemester
 	 */
 	public Boolean isFirstSemester() {
 		return isFirstSemester;
 	}
 	/**
 	 * @param isFirstSemester the isFirstSemester to set
 	 */
 	public Course isFirstSemester(Boolean isFirstSemester) {
 		this.isFirstSemester = isFirstSemester;
 		return this;
 	}
 	/**
 	 * 取得 结课考核成绩
 	 * @return 结课考核成绩
 	 */
 	public short getTestScore() {
 		return testScore;
 	}
 	/**
 	 * 设置 结课考核成绩
 	 * @param testScore 结课考核成绩  to set
 	 * @throws CourseException when testScore<0 || testScore>999
 	 */
 	public Course setTestScore(int testScore) throws CourseException {
 		if(testScore<0 || testScore>999)
 			throw new CourseException("Illegel score of test!");
 		this.testScore = (short) testScore;
 		return this;
 	}
 	/**
 	 * 取得 期末总评成绩
 	 * @return 期末总评成绩
 	 */
 	public short getTotalScore() {
 		return totalScore;
 	}
 	/**
 	 * 设置 期末总评成绩
 	 * @param totalScore 期末总评成绩 to set
 	 * @throws CourseException when totalScore<0 || totalScore>999
 	 */
 	public Course setTotalScore(int totalScore) throws CourseException {
 		if(totalScore<0 || totalScore>999)
 			throw new CourseException("Illegel final score!");
 		this.totalScore = (short) totalScore;
 		return this;
 	}
 	/**
 	 * @return the kind
 	 */
 	public String getKind() {
 		return kind;
 	}
 	/**
 	 * @param kind the kind to set
 	 */
 	public Course setKind(String kind) {
 		this.kind = kind;
 		return this;
 	}
 	
 	/**
 	 * 增加教师，可以一次添加多名，用[,，;；]分开
 	 * @param teacher 教师名，可以是多名教师，用[,，;；]分开
 	 * @return 参数合法返回this（builder），参数非法抛出异常
 	 * @throws NullPointerException 参数teacher是null
 	 */
 	public Course addTeacher(String teacher){
 		if(teacher == null)
 			throw new NullPointerException("teacher shouldn't haven been null when addTeacher.");
 		String[] teachers = teacher.split("[,，;；]");
 		for(String t:teachers){
 			t = t.trim();
 			if(t.length()>0)
 				this.teachers.add(t);
 		}
 		return this;
 	}
 	/**
 	 * 返回教师名称，若有多名教师，用“,”隔开。
 	 * @return 类似teacher1,teacher2,teacher3的字符串；若为空，返回null
 	 */
 	public String getTeacherString(){
 		StringBuilder sb = new StringBuilder();
 		for(String teacher:this.teachers)
 			sb.append(teacher+",");
 		if(sb.length() == 0)
 			return null;
 		else
 			return sb.substring(0, sb.length()-1);
 	}
 	
 	/**
 	 * 以字符串形式，增加时间和对应的地点
 	 * @param weeks 周次，类似2,4,6,8,10,12,14,18-20的字符串
 	 * @param daysOfWeek 星期(day of week)，类似“星期一 至 星期四 星期六”的字符串
 	 * @param periods 课时（第几节课），类似2,4,6,8,10,12 10-13的字符串
 	 * @param address 对应上面确定的时间的 上课地址
 	 * @return 参数合法返回this（builder），参数非法抛出异常
 	 * @throws TimeAndAddressException
 	 * @throws BitOperateException
 	 */
 	public Course addTimeAndAddress(String weeks, String daysOfWeek, String periods, 
 			String address) throws TimeAndAddressException, BitOperateException{
 		timeAndAddress.add(new TimeAndAddress(weeks, daysOfWeek, periods, address));
 		return this;
 	}
 	/**
 	 * 以字符串形式，增加时间和对应的地点
 	 * @param weeks 周次，类似2,4,6,8,10,12,14,18-20的字符串
 	 * @param oddOrEvenWeeks true表仅新增单周，false表仅双周，null表连续（不分单双周）。如1-11奇数周应设为true
 	 * @param daysOfWeek 星期(day of week)，类似“星期一 至 星期四 星期六”的字符串
 	 * @param periods 课时（第几节课），类似2,4,6,8,10,12 10-13的字符串
 	 * @param address 对应上面确定的时间的 上课地址
 	 * @return 参数合法返回this（builder），参数非法抛出异常
 	 * @throws TimeAndAddressException
 	 * @throws BitOperateException
 	 */
 	public Course addTimeAndAddress(String weeks, Boolean oddOrEvenWeeks, String daysOfWeek, 
 				String periods, String address) throws TimeAndAddressException, BitOperateException{
 		timeAndAddress.add(new TimeAndAddress(weeks,oddOrEvenWeeks,daysOfWeek,periods,address));
 		return this;
 	}
 	/**
 	 * 把timeAndAddress添加到本Course中
 	 * @param timeAndAddress 要新加入的时间和对应地点
 	 * @return 参数合法返回this（builder），参数非法抛出异常
 	 * @throws CourseException
 	 */
 	public Course addTimeAndAddress(TimeAndAddress timeAndAddress) throws CourseException{
 		if(timeAndAddress == null)
 			throw new CourseException("timeAndAddress shouldn't have been null.");
 		this.timeAndAddress.add(new TimeAndAddress(timeAndAddress));
 		return this;
 	}
 	/**
 	 * 返回时间地点，若有多组，用“\n”隔开。
 	 * @return 类似“1-2,5-16 周 星期四 9-10 节 1-0301” 的字符串；若为空，返回null
 	 */
 	public String getTimeAndAddressString(){
 		StringBuilder sb = new StringBuilder();
 		for(TimeAndAddress taa:this.timeAndAddress)
 			sb.append(taa.toString()+"\n");
 		if(sb.length() == 0)
 			return null;
 		else
 			return sb.substring(0, sb.length()-1);
 	}
 	@Override
 	public String toString(){
 		return getCode()+"\t"+getName()+"\t"+getClassNumber()+"\t"+getTeacherString()+"\t"
 				+getCredit()+"\t"+getTestScore()+"\t"+getTotalScore()+"\t"+getYear()+"\t"
 				+isFirstSemester()+"\t"+getKind()+"\t"+getNote()+"\n"+getTimeAndAddressString();
 	}
 
 	public static class CourseException extends Exception{
 		private static final long serialVersionUID = -4494349664328514829L;
 		public CourseException(String message){
 			super(message);
 		}
 		public CourseException(){
 			super("Encounter exception in Course class");
 		}
 	}
 
 
 	public static class TimeAndAddress{
 		/**周<br />第0-20位分别表第0-20周*/
 		private int week;
 		/**星期 day of week<br />第0位表周日，1-6位表周一到周六*/
 		private byte day;
 		/**第几节课<br />第1-13位分别表第1-13节课*/
 		private short period;
 		/**地点*/
 		private String address;
 		
 		/**
 		 * 以字符串设置各个属性
 		 * @param weeks 周次，类似2,4,6,8,10,12,14,18-20的字符串
 		 * @param daysOfWeek 星期(day of week)，类似“星期一 至 星期四 星期六”的字符串
 		 * @param periods 课时（第几节课），类似2,4,6,8,10,12 10-13的字符串
 		 * @param address 对应上面确定的时间的 上课地址
 		 * @throws TimeAndAddressException
 		 * @throws BitOperateException
 		 */
 		public TimeAndAddress(String weeks, String daysOfWeek, String periods, String address) 
 				throws TimeAndAddressException, BitOperateException{
 			this();
 			addWeeks(weeks).addDays(daysOfWeek).addPeriods(periods).setAddress(address);
 		}
 		/**
 		 * 以字符串设置各个属性
 		 * @param weeks 周次，类似2,4,6,8,10,12,14,18-20的字符串
 		 * @param oddOrEvenWeeks true表仅新增单周，false表仅双周，null表连续（不分单双周）。如1-11奇数周应设为true
 		 * @param daysOfWeek 星期(day of week)，类似“星期一 至 星期四 星期六”的字符串
 		 * @param periods 课时（第几节课），类似2,4,6,8,10,12 10-13的字符串
 		 * @param address 对应上面确定的时间的 上课地址
 		 * @throws TimeAndAddressException
 		 * @throws BitOperateException
 		 */
 		public TimeAndAddress(String weeks, Boolean oddOrEvenWeeks, String daysOfWeek, 
 				String periods, String address) throws TimeAndAddressException, BitOperateException{
 			this();
 			addWeeks(weeks, oddOrEvenWeeks).addDays(daysOfWeek).addPeriods(periods).setAddress(address);
 		}
 		/**
 		 * 全参构造方法，若真有必要用此构造函数，建议数字用16进制表示(0x开头)
 		 * @param weekNumber 类似dayOfWeek，0-20位的1/0表示是否有这一周。如16进制0x00 00 01 02 表示8、1周有课 
 		 * @param dayOfWeek 0-6位分别表示周日到周六，1表有这一天，0表无。如二进制0010 0010表示星期一、星期五
 		 * @param period 类似上面，1-13为表1-13节课
 		 * @param address 对应上面时间的地点		 
 		 * @throws BitOperateException 
 		 * @throws TimeAndAddressException */
 		public TimeAndAddress(int weekNumber,int dayOfWeek,int period,String address) 
 				throws BitOperateException, TimeAndAddressException{
 			//this();	//有没有必要呢
 			setWeek(weekNumber).setDay(dayOfWeek).setPeriod(period).setAddress(address);
 		}
 		public TimeAndAddress(String address) throws TimeAndAddressException{
 			this();
 			setAddress(address);
 		}
 		public TimeAndAddress(){
 			this.week = 0;
 			this.day = 0;
 			this.period = 0;
 			this.address = "";
 		}
 		/**拷贝构造方法*/
 		public TimeAndAddress(TimeAndAddress aCourseTime){
 			week = aCourseTime.week;
 			day = aCourseTime.day;
 			period = aCourseTime.period;
 			address = aCourseTime.address;
 		}
 		
 		
 	    
 	    /**
 	     * 类似getDay，0-20位的1/0表示是否有这一周。如16进制0x00 00 01 02 表示8、1周有课 
 		 * @return the week
 		 */
 		public int getWeek() {
 			return week;
 		}
 		/**
 		 * 类似setDay，0-20位的1/0表示是否有这一周。如16进制0x00 00 01 02 表示8、1周有课 
 		 * @param week the week to set
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress setWeek(int week) throws BitOperateException {
 			if(BitOperate.has1onBitsHigherThan(week, 21))
 				throw new BitOperateException("Too large week number.(>=21)", 
 						BitOperateException.TOO_LARGE_POSITON);
 			this.week = week;
 			return this;
 		}
 		/**
 		 * 返回值0-6位分别表示周日到周六，1表有这一天，0表无。如二进制0010 0010表示星期一、星期五
 		 * @return the day of week
 		 */
 		public byte getDay() {
 			return day;
 		}
 		/**
 		 * 参数的0-6位分别表示周日到周六，1表有这一天，0表无。如二进制0010 0010表示星期一、星期五
 		 * @param day the day of week to set
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress setDay(int dayOfWeek) throws BitOperateException {
 			if(BitOperate.has1onBitsHigherThan(dayOfWeek, 8))
 				throw new BitOperateException("Too large day of week.(>=8)", 
 						BitOperateException.TOO_LARGE_POSITON);
 			if(BitOperate.is1onCertainBit(dayOfWeek, 7)){
 				dayOfWeek = BitOperate.add1onCertainBit(dayOfWeek, 0);
 				dayOfWeek = BitOperate.add0onCertainBit(dayOfWeek, 7);
 			}
 			this.day = (byte)dayOfWeek;
 			return this;
 		}
 		/**
 		 * 类似getDay，1-13为表1-13节课
 		 * @return the period
 		 */
 		public short getPeriod() {
 			return period;
 		}
 		/**
 		 * 类似setDay，1-13为表1-13节课
 		 * @param period the period to set
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress setPeriod(int period) throws BitOperateException {
 			if(BitOperate.has1onBitsHigherThan(period, 14))
 				throw new BitOperateException("Too large period number.(>13)", 
 						BitOperateException.TOO_LARGE_POSITON);
 			if(BitOperate.is1onCertainBit(period, 0))
 				throw new BitOperateException("Illegal period: 0.", 
 						BitOperateException.ILLEGAL_POSITION);
 			this.period = (short)period;
 			return this;
 		}
 		/**
 		 * @return the address
 		 */
 		public String getAddress() {
 			return address;
 		}
 		/**
 		 * @param address the address to set
 		 * @throws TimeAndAddressException when address is null
 		 */
 		public TimeAndAddress setAddress(String address) throws TimeAndAddressException {
 			if(address == null)
 				throw new TimeAndAddressException("Shouldn't set address to null.");
 			this.address = address;
 			return this;
 		}
 		
 		/**
 		 * 增加周次，识别类似2,4,6,8,10,12,14,18 01-20的字符串<br />
 		 * <strong>注意：</strong>2,4,6,8,10,12,14,18 01- 20（注意最后一个空格）无效
 		 * @param weeksStr 类似2,4,6,8,10,12,14,18 01-21的字符串
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 * @precondition weeksStr不包含[\\d\\s\u00a0\u3000;；,，\\-－\u2013\u2014\u2015]以外的任何内容
 		 */
 		public TimeAndAddress addWeeks(String weeksStr) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.week, weeksStr);
 			return setWeek(result);
 		}
 		/**
 		 * 增加周次，识别类似2,4,6,8,10,12,14,18 01-20的字符串<br />
 		 * <strong>注意：</strong>2,4,6,8,10,12,14,18 01- 20（注意最后一个空格）无效
 		 * @param weeksStr 类似2,4,6,8,10,12,14,18 01-21的字符串
 		 * @param oddOrEven true表仅新增单周，false表仅双周，null表连续（不分单双周）。如1-11奇数周应设为true
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 * @precondition weeksStr不包含[\\d\\s\u00a0\u3000;；,，\\-－\u2013\u2014\u2015]以外的任何内容
 		 */
 		public TimeAndAddress addWeeks(String weeksStr, Boolean oddOrEven) 
 				throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.week, weeksStr, oddOrEven);
 			return setWeek(result);
 		}
 		/**
 		 * 增加一定范围的周次，可以设置单双周。例如增加1-11周的奇数周
 		 * @param start 起始周次(0-20)，包括start。如1-11周，start为1
 		 * @param end 结束周次(0-20)，包括end。如1-11周，end为11
 		 * @param oddOrEven true表仅单周，false表仅双周，null表连续（不分单双周）。如1-11奇数周应设为true
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress addWeeksOnRange(int start, int end, Boolean oddOrEven) throws BitOperateException{
 			int result = BitOperate.add1onRange(week, start, end, oddOrEven);
 			return setWeek(result);
 		}
 		/**
 		 * 增加一定范围的周次。例如增加1-12周
 		 * @param start 起始周次(0-20)，包括start。如1-12周，start为1
 		 * @param end 结束周次(0-20)，包括end。如1-12周，end为12
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress addWeeksOnRang(int start, int end) throws BitOperateException{
 			return addWeeksOnRange(start, end, null);
 		}
 		/**
 		 * 增加周次
 		 * @param week 0-20分别表示0-20周
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress addWeek(int week) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.week, week);
 			return setWeek(result);
 		}
 		/**
 		 * 批量增加周次
 		 * @param weeks 0-20分别表示0-20周
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress addWeeks(int[] weeks) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.week, weeks);
 			return setWeek(result);
 		}
 		/**
 		 * 检查是否设置了某周，已设置返回true
 		 * @param testedWeek 被检测的周数
 		 * @return 若设置了此周返回true，没有设置返回false
 		 * @throws BitOperateException when testedWeek<0 || testedWeek>20
 		 */
 		public boolean hasSetWeek(int testedWeek) throws BitOperateException{
 			if(testedWeek>20)
 				throw new BitOperateException("Too large week number.(>20)", 
 						BitOperateException.TOO_LARGE_POSITON);
 			return BitOperate.is1onCertainBit(this.week, testedWeek);
 		}
 		/**
 		 * 以字符串返回周数，和addWeeks(String weeksStr)相反
 		 * @return 类似 “1,5,7,9,11,13-15 周” 的字符串
 		 */
 		public String getWeekString(){
 			return BitOperate.convertIntToString(week) + " 周";
 		}
 		
 		/**
 		 * 增加星期(day of week)，识别类似“星期一 至 星期五”的字符串
 		 * @param daysStr 类似“星期一 至 星期五”的字符串
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @precondition 不含“\\s\u00a0\u3000;；,，星期周日一二三四五六至到”以外字符
 		 * @throws BitOperateException
 		 * @throws TimeAndAddressException 参数大于7、不可识别星期、至/到位置错误
 		 */
 		public TimeAndAddress addDays(String daysStr) 
 				throws BitOperateException, TimeAndAddressException{
 			if(daysStr.matches(".*[^\\s\u00a0\u3000;；,，星期周日一二三四五六至到].*"))
 				throw new BitOperateException("Unknown character in parameter.", 
 						BitOperateException.UNKNOWN_NOTATION);
 			daysStr = daysStr.trim();
 			String[] days = daysStr.split("[\\s\u00a0\u3000;；,，]");	//根据这些分割
 			ArrayList<String> daysList = new ArrayList<String>(days.length);
 			for(String day:days)
 				if(day.length()>0)
 					daysList.add(day);
 			daysList.trimToSize();
 			setDay(HandleComplexDays(daysList));
 			return this;
 		}
 		private int HandleComplexDays(ArrayList<String> complexDaysStr)
 				throws TimeAndAddressException, BitOperateException{
 			boolean justDone = false;
 			int result = this.day, index = 0, start, end;
 			while( index < complexDaysStr.size() ){
 				//没有遇到[至到]，继续
 				if(!complexDaysStr.get(index).matches(".*[至到].*")){
 					justDone = false;
 					index++;
 					continue;
 				}
 				//遇到[至到]
 				if(justDone && !complexDaysStr.get(index).matches(".+[至到]"))//连续遇到[至到]
 					throw new TimeAndAddressException("consequent [至到].");
 				//正常遇到[至到]
 				try{
 					if(complexDaysStr.get(index).matches(".+[至到].+")){
 						String[] days = complexDaysStr.get(index).split("[至到]");
 						if(days.length != 2)
 							throw new TimeAndAddressException("consequent [至到].");
 						start = dayStringToNumber(days[0]);
 						end = dayStringToNumber(days[1]);
 						complexDaysStr.remove(index);
 					}else if(complexDaysStr.get(index).matches(".+[至到]")){
 						start = dayStringToNumber(complexDaysStr.get(index));
 						end = dayStringToNumber(complexDaysStr.get(index+1));
 						complexDaysStr.remove(index);
 						complexDaysStr.remove(index);
 					}else if(complexDaysStr.get(index).matches("[至到].+")){
 						start = dayStringToNumber(complexDaysStr.get(index-1));
 						end = dayStringToNumber(complexDaysStr.get(index));
 						index--;
 						complexDaysStr.remove(index);
 						complexDaysStr.remove(index);
 					}else{
 						start = dayStringToNumber(complexDaysStr.get(index-1));
 						end = dayStringToNumber(complexDaysStr.get(index+1));
 						index--;
 						complexDaysStr.remove(index);	//移去处理过的
 						complexDaysStr.remove(index);
 						complexDaysStr.remove(index);
 					}
 					if(end == 0 && end<start)	//星期* 至 星期日
 						end = 7;
 					result = BitOperate.add1onRange(result, start, end);
 					justDone = true;
 				}catch(IndexOutOfBoundsException e){
 					throw new TimeAndAddressException("Wrong position of [至到].\n"+e.toString());
 				}
 			}
 			//daysList里应当没有[至到]了
 			for(String day:complexDaysStr)
 				result = BitOperate.add1onCertainBit(result, dayStringToNumber(day));
 			return result;
 		}
 		public static int dayStringToNumber(String weekStr) throws TimeAndAddressException{
 			if(weekStr == null)
 				throw new TimeAndAddressException("weekStr is NULL.");
 			if(weekStr.matches(".*日.*"))
 				return 0;
 			if(weekStr.matches(".*一.*"))
 				return 1;
 			if(weekStr.matches(".*二.*"))
 				return 2;
 			if(weekStr.matches(".*三.*"))
 				return 3;
 			if(weekStr.matches(".*四.*"))
 				return 4;
 			if(weekStr.matches(".*五.*"))
 				return 5;
 			if(weekStr.matches(".*六.*"))
 				return 6;
 			throw new TimeAndAddressException("Unknown Chinese day of week string: "+weekStr);
 		}
 		/**
 		 * 增加一定范围的星期(day of week)，可以设置单双星期。例如增加周一至周五的奇数周
 		 * @param start 起始星期(0-7)，包括start，0、7都表周日。如周一至周五，start为1
 		 * @param end 结束星期(0-7)，包括end，0、7都表周日。如周一至周五，end为5
 		 * @param oddOrEven true表仅单数星期，false表仅双数星期，null表连续（不分单双星期）。
 		 * 如周一至周五奇数星期应设为true <strong><em>注：</em></strong>周日用0表示为双数，用7表示为单数
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress addDaysOnRange(int start, int end, Boolean oddOrEven) throws BitOperateException{
 			int result = BitOperate.add1onRange(day, start, end, oddOrEven);
 			return setDay(result);
 		}
 		/**
 		 * 增加一定范围的星期(day of week)。例如增加周一至周五
 		 * @param start 起始星期(0-7)，包括start，0、7都表周日。如周一至周五，start为1
 		 * @param end 结束星期(0-7)，包括end，0、7都表周日。如周一至周五，end为5
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress addDaysOnRang(int start, int end) throws BitOperateException{
 			return addDaysOnRange(start, end, null);
 		}
 		/**
 		 * 增加星期
 		 * @param dayOfWeek 0 、7表周日，1-6表周一到周六
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress addDay(int dayOfWeek) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.day, dayOfWeek);
 			return setDay(result);
 		}
 		/**
 		 * 批量增加星期
 		 * @param daysOfWeek 0、7表周日，1-6表周一到周六
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress addDays(int[] daysOfWeek) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.day, daysOfWeek);
 			return setDay(result);
 		}
 		/**
 		 * 检查是否设置了某星期(day of week),，已设置返回true
 		 * @param testedDayOfWeek 被检查的星期数，0、7都表示周日，1-6分别表示周一~周六
 		 * @return 若设置了此星期返回true，没有设置返回false
 		 * @throws BitOperateException when testedWeek<0 || testedWeek>7
 		 */
 		public boolean hasSetDay(int testedDayOfWeek) throws BitOperateException{
 			if(testedDayOfWeek > 7)
 				throw new BitOperateException("Too large day of week number.(>7)", 
 						BitOperateException.TOO_LARGE_POSITON);
 			if(testedDayOfWeek == 7)
 				testedDayOfWeek = 0;
 			return BitOperate.is1onCertainBit(this.day, testedDayOfWeek);
 		}
 		/**
 		 * 以字符串返回星期*或者周*，和addDays(String daysStr)相反
 		 * @param xingqiOrZhou 若为true返回星期*,若为false返回周*
 		 * @return 类似 “星期一,星期三 至 星期五” 或者 "周一，周三 至 周五" 的字符串
 		 */
 		public String getDayString(boolean xingqiOrZhou){
 			String prefix = xingqiOrZhou?"星期":"周";
 			String result = BitOperate.convertIntToString(day);
 			result = result.replace("0", prefix+"日");
 			result = result.replace("1", prefix+"一");
 			result = result.replace("2", prefix+"二");
 			result = result.replace("3", prefix+"三");
 			result = result.replace("4", prefix+"四");
 			result = result.replace("5", prefix+"五");
 			result = result.replace("6", prefix+"六");
 			result = result.replace("-", " 至 ");
 			return result;
 		}
 		/**
 		 * 以字符串返回星期*，和addDays(String daysStr)相反
 		 * @return 类似 “星期一,星期三 至 星期五” 的字符串
 		 */
 		public String getDayString(){
 			return getDayString(true);
 		}
 
 		/**
 		 * 增加课时（第几节课），识别类似2,4,6,8,10,12 10-13的字符串<br />
 		 * <strong>注意：</strong>2,4,6,8,10,12 10- 13（注意最后一个空格）无效
 		 * @param periodsStr 类似2,4,6,8,10,12 10-13的字符串
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException
 		 * @precondition periodsStr不包含[\\d\\s\u00a0\u3000;；,，\\-－\u2013\u2014\u2015]以外的任何内容
 		 */
 		public TimeAndAddress addPeriods(String periodsStr) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.period, periodsStr);
 			return setPeriod(result);
 		}
 		/**
 		 * 增加课时（第几节课）
 		 * @param bigPeriod 1-6分别表1-2节、3-4节、5-6节、7-8节、9-10节、11-13节
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws Exception 
 		 */
 		public TimeAndAddress addPeriods(int bigPeriod) throws TimeAndAddressException{
 			try{
 				switch(bigPeriod){
 				case 1:addPeriods(new int[]{1,2});break;
 				case 2:addPeriods(new int[]{3,4});break;
 				case 3:addPeriods(new int[]{5,6});break;
 				case 4:addPeriods(new int[]{7,8});break;
 				case 5:addPeriods(new int[]{9,10});break;
 				case 6:addPeriods(new int[]{11,12,13});break;
 				default:throw new TimeAndAddressException("Illegal bigPeriod number.");
 				}
 			}catch(BitOperateException bitOE){
 				bitOE.printStackTrace();
 			}
 			return this;
 		}
 		/**
 		 * 增加一定范围的课时（第几节课），可以设置单双课时。例如增加1-11节的奇数节课
 		 * @param start 起始课时(1-13)，包括start。如1-11节，start为1
 		 * @param end 结束课时(1-13)，包括end。如1-11节，end为11
 		 * @param oddOrEven true表仅单数节课，false表仅双数节，null表连续（不分单双节）。如1-11奇数节应设为true
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress addPeriodsOnRange(int start, int end, Boolean oddOrEven) throws BitOperateException{
 			int result = BitOperate.add1onRange(period, start, end, oddOrEven);
 			return setPeriod(result);
 		}
 		/**
 		 * 增加一定范围的课时（第几节课）。例如增加1-12节课
 		 * @param start 起始课时(1-13)，包括start。如1-12节课，start为1
 		 * @param end 结束课时(1-13)，包括end。如1-12节课，end为12
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress addPeriodsOnRang(int start, int end) throws BitOperateException{
 			return addPeriodsOnRange(start, end, null);
 		}
 		/**
 		 * 增加课时（第几节课）
 		 * @param period 1-13分别表第1到13节课
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress addPeriod(int period) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.period, period);
 			return setPeriod(result);
 		}
 		/**
 		 * 批量增加课时（第几节课）
 		 * @param periods 1-13分别表第1到13节课
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 
 		 */
 		public TimeAndAddress addPeriods(int[] periods) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(period, periods);
 			return setPeriod(result);
 		}
 		/**
 		 * 检查是否设置了某节课，已设置返回true
 		 * @param testedPeriod 被检测的周数，1-13分别表示1-13节课
 		 * @return 若设置了此周返回true，没有设置返回false
 		 * @throws BitOperateException when testedWeek<1 || testedWeek>13
 		 */
 		public boolean hasSetPeriod(int testedPeriod) throws BitOperateException{
 			if(testedPeriod>13)
 				throw new BitOperateException("Too large period number.(>13)", 
 						BitOperateException.TOO_LARGE_POSITON);
 			if(testedPeriod<1)
 				throw new BitOperateException("Too small period number.(<1)");
 			return BitOperate.is1onCertainBit(this.period, testedPeriod);
 		}
 		/**
 		 * 以字符串返回周数，和addPeriods(String periodsStr)相反
 		 * @return 类似 “3-4,5,7,9,11-13 节” 的字符串
 		 */
 		public String getPeriodString(){
 			return BitOperate.convertIntToString(period) + " 节";
 		}
 		
 		public String toString(){
 			return getWeekString()+" "+getDayString()+" "+getPeriodString()+" "+getAddress();
 		}
 		
 		public static class TimeAndAddressException extends Exception{
 			private static final long serialVersionUID = 1900908778609873214L;
 			public TimeAndAddressException(String message){
 				super(message);
 			}
 			public TimeAndAddressException(){
 				super("Encounter Exception in class TimeAndAddress.");
 			}
 		}
 	}
 }

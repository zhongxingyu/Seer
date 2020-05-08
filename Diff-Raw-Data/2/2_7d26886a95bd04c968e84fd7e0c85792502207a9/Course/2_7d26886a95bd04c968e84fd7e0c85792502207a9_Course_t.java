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
 public class Course implements Cloneable{
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
 	
 	/**时间地点列表*/
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
 	private float testScore;
 	/**期末总评成绩*/
 	private float totalScore;
 	/**课程性质*/
 	private String kind;
 	
 
 	/**
 	 * 无参构造方法，各属性设为默认空值
 	 */
 	public Course(){
 		id = 0;
 		code = name = classNumber = teachingMaterial = note = kind= null;
 		credit = 0;
 		year = 0;
 		isFirstSemester = null;
 		testScore = totalScore = Float.NaN;
 		teachers = new ArrayList<String>();
 		timeAndAddress = new ArrayList<TimeAndAddress>();
 	}
 	/**
 	 * @param code 课程代码
 	 * @param name 课程名称
 	 * @param credit 课程学分
 	 * @param classNumber 教学班号
 	 * @throws CourseException credit<=0,或者credit大于Byte.MAX_VALUE
 	 */
 	public Course(String code, String name, int credit, String classNumber) throws CourseException{
 		this();
 		this.code = code;
 		this.name = name;
 		setCredit(credit);
 		this.classNumber = classNumber;
 	}
 	/**
 	 * @param code 课程代码
 	 * @param name 课程名称
 	 * @param credit 课程学分
 	 * @param classNumber 教学班号
 	 * @param testScore 结课考核成绩
 	 * @param totalScore 期末总评成绩
 	 * @param year 学年
 	 * @param isFirstSemester 学期。true表示上半学期，false表示下半学期，null表示未知
 	 * @throws CourseException credit<=0,或者credit大于Byte.MAX_VALUE 或者 成绩超出0~999的范围 或者 学年year超出1900~9999的范围
 	 */
 	public Course(String code, String name, int credit, String classNumber, int testScore, 
 			int totalScore, int year, Boolean isFirstSemester) throws CourseException{
 		this(code, name, credit, classNumber);
 		setTestScore(testScore);
 		setTotalScore(totalScore);
 		setYear(year);
 		this.isFirstSemester = isFirstSemester;
 	}
 	/**全参构造方法
 	 * @throws CourseException 请见{@link #Course(String, String, int, String, int, int, int, Boolean)}*/
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
 	public Course(Course src){
 		this();
 		this.code = src.code;
 		this.name = src.name;
 		this.credit = src.credit;
 		this.classNumber = src.classNumber;
 		this.testScore = src.testScore;
 		this.totalScore = src.totalScore;
 		this.year = src.year;
 		this.isFirstSemester = src.isFirstSemester;
 		
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
 	 * @return 课程代码
 	 */
 	public String getCode() {
 		return code;
 	}
 	/**
 	 * @param code 课程代码
 	 */
 	public Course setCode(String code) {
 		this.code = code;
 		return this;
 	}
 	/**
 	 * @return 课程名称
 	 */
 	public String getName() {
 		return name;
 	}
 	/**
 	 * @param name 课程名称
 	 */
 	public Course setName(String name) {
 		this.name = name;
 		return this;
 	}
 	/**
 	 * 取得教师列表。为方便，可直接对此方法返回的引用操作，对之的操作会作用在此Course对象上
 	 * @return 授课教师的ArrayList
 	 */
 	public ArrayList<String> getTeachers() {
 		return teachers;
 	}
 	/**
 	 * 设置授课教师列表。把本Course对象的teachers清空并设置为参数指定的内容。之后对参数teachers的修改不会作用到此Course。
 	 * @param teachers 授课教师的ArrayList。如果为null或为空ArrayList或teachers.get(0)为null，则仅清空教师信息
 	 */
 	public Course setTeachers(ArrayList<String> teachers){
 		this.teachers.clear();
 		if(teachers != null && !teachers.isEmpty() && teachers.get(0) != null)
 			this.teachers.addAll(teachers);
 		return this;
 	}
 	/**
 	 * 把本Course对象的teachers清空并设置为参数指定的内容
 	 * @param teachers 授课教师列表的String[]。如果为null或长度为0，则仅清空教师信息
 	 */
 	public Course setTeachers(String[] teachers){
 		this.teachers.clear();
 		if(teachers != null && teachers.length != 0)
 			for(String teacher:teachers)
 				if(teacher != null)
 					this.teachers.add(teacher);
 		return this;
 	}
 	/**
 	 * 把本Course对象的teachers清空并设置为参数指定的内容
 	 * @param teachers 教师名，可以是多名教师，用[,，;；]分开。如果为null，则仅清空教师信息
 	 */
 	public Course setTeachers(String teachers){
 		this.teachers.clear();
 		if(teachers != null)
 			addTeacher(teachers);
 		return this;
 	}
 	/**
 	 * @return 学分
 	 */
 	public byte getCredit() {
 		return credit;
 	}
 	/**
 	 * @param credit 学分
 	 * @throws CourseException credit<0,或者credit大于Byte.MAX_VALUE
 	 */
 	public Course setCredit(int credit) throws CourseException {
 		if(credit<0)
 			throw new CourseException("Illegal credit: "+credit+". Credit should have been positive.");
 		else if(credit>Byte.MAX_VALUE)
 			throw new CourseException("Illegal credit: "+credit+". Credit should have been less than "+Byte.MAX_VALUE+".");
 		this.credit = (byte)credit;
 		return this;
 	}
 	/**
 	 * @return 教学班号
 	 */
 	public String getClassNumber() {
 		return classNumber;
 	}
 	/**
 	 * @param classNumber 教学班号
 	 */
 	public Course setClassNumber(String classNumber) {
 		this.classNumber = classNumber;
 		return this;
 	}
 	/**
 	 * 取得时间地点列表。为方便，可直接对此方法返回的引用操作，对之的操作会作用在此Course对象上
 	 * @return 时间地点列表
 	 */
 	public ArrayList<TimeAndAddress> getTimeAndAddress() {
 		return timeAndAddress;
 	}
 	/**
 	 * 把本Course对象的TimeAndAddresses清空并设置为参数指定的内容。拷贝参数timeAndAddresses，以后对之的修改不影响此Course对象
 	 * @param timeAndAddresses 时间地点列表。如果为null或为空ArrayList，则仅清空时间地点信息
 	 */
 	public Course setTimeAndAddresse(ArrayList<TimeAndAddress> timeAndAddresses){
 		this.timeAndAddress.clear();
 		if(timeAndAddresses!=null && !timeAndAddresses.isEmpty())
 			for(TimeAndAddress TA:timeAndAddresses)
 				if(TA != null)
 					this.timeAndAddress.add(new TimeAndAddress(TA));
 		return this;
 	}
 	/**
 	 * 把本Course对象的TimeAndAddresses清空并设置为参数指定的内容。拷贝参数timeAndAddresses，以后对之的修改不影响此Course对象
 	 * @param timeAndAddresses 时间地点列表。若果为null或长度为0，则仅清空时间地点信息
 	 */
 	public Course setTimeAndAddresse(TimeAndAddress[] timeAndAddresses){
 		this.timeAndAddress.clear();
 		if(timeAndAddresses!=null && timeAndAddresses.length!=0)
 			for(TimeAndAddress TA:timeAndAddresses)
 				if(TA != null)
 					this.timeAndAddress.add(new TimeAndAddress(TA));
 		return this;
 	}
 	/**
 	 * @return 参考教材
 	 */
 	public String getTeachingMaterial() {
 		return teachingMaterial;
 	}
 	/**
 	 * @param teachingMaterial 参考教材
 	 */
 	public Course setTeachingMaterial(String teachingMaterial) {
 		this.teachingMaterial = teachingMaterial;
 		return this;
 	}
 	/**
 	 * @return 备注
 	 */
 	public String getNote() {
 		return note;
 	}
 	/**
 	 * @param note 备注
 	 */
 	public Course setNote(String note) {
 		this.note = note;
 		return this;
 	}
 	/**
 	 * @return 学年
 	 */
 	public short getYear() {
 		return year;
 	}
 	/**
 	 * @param year 学年。
 	 * @throws CourseException when (year!=0 && year<1900) || year>9999
 	 */
 	public Course setYear(int year) throws CourseException {
 		if( (year!=0 && year<1900) || year>9999)
 			throw new CourseException("Illegal year: "+year);
 		this.year = (short) year;
 		return this;
 	}
 	/**
 	 * @return 学期。true表示上半学期，false表示下半学期，null表示未知
 	 */
 	public Boolean isFirstSemester() {
 		return isFirstSemester;
 	}
 	/**
 	 * @param isFirstSemester 学期。true表示上半学期，false表示下半学期，null表示未知
 	 */
 	public Course setIsFirstSemester(Boolean isFirstSemester) {
 		this.isFirstSemester = isFirstSemester;
 		return this;
 	}
 	/**
 	 * 取得 结课考核成绩
 	 * @return 结课考核成绩。默认值{@link Float#NaN}表示未设置
 	 */
 	public float getTestScore() {
 		return testScore;
 	}
 	/**
 	 * 设置 结课考核成绩
 	 * @param testScore 结课考核成绩 
 	 * @throws CourseException when isInfinite(testScore) || isNaN(testScore) || testScore<0 || testScore>999
 	 */
 	public Course setTestScore(float testScore) throws CourseException {
 		if(Float.isInfinite(testScore) || Float.isNaN(testScore) || testScore<0 || testScore>999)
 			throw new CourseException("Illegel score of test: "+testScore);
 		this.testScore = testScore;
 		return this;
 	}
 	/**
 	 * 取得 期末总评成绩
 	 * @return 期末总评成绩。默认值{@link Float#NaN}表示未设置
 	 */
 	public float getTotalScore() {
 		return totalScore;
 	}
 	/**
 	 * 设置 期末总评成绩
 	 * @param totalScore 期末总评成绩 
 	 * @throws CourseException when isInfinite(totalScore) || isNaN(totalScore) || totalScore<0 || totalScore>999
 	 */
 	public Course setTotalScore(float totalScore) throws CourseException {
 		if(Float.isInfinite(totalScore) || Float.isNaN(totalScore) || totalScore<0 || totalScore>999)
 			throw new CourseException("Illegel final score: "+totalScore);
 		this.totalScore = totalScore;
 		return this;
 	}
 	/**
 	 * 返回分数score对应的的绩点
 	 * @param score 要计算的分数
 	 * @return 分数score对应的绩点。0-59的绩点为0
 	 * @throws CourseException when isInfinite(score) || isNaN(score) || score<0 || score>100
 	 */
 	public static float getGradePoint(float score) throws CourseException{
 		if(Float.isInfinite(score) || Float.isNaN(score) || score<0 || score>100)
 			throw new CourseException("Illegel score: "+score);
 		if(score<60)
 			return 0;
 		else if(score<65)
 			return 1;
 		else if(score<70)
 			return 1.5f;
 		else if(score<75)
 			return 2;
 		else if(score<80)
 			return 2.5f;
 		else if(score<85)
 			return 3;
 		else if(score<90)
 			return 3.5f;
 		else if(score<96)
 			return 4;
 		else
 			return 4.5f;
 	}
 	/**
 	 * 取得绩点。若fromTotalScoreOrTestScore为真，从期末总评成绩计算；否则从结课考核成绩计算
 	 * @param fromTotalScoreOrTestScore 如果是true，从期末总评成绩计算绩点；如果是false，从结课考核成绩计算
 	 * @return 指定成绩的绩点。0-59分的绩点为0
 	 * @throws CourseException 当  尚未设置相关成绩  时
 	 */
 	public float getGradePoint(boolean fromTotalScoreOrTestScore) throws CourseException{
 		float score;
 		if(fromTotalScoreOrTestScore)
 			score = getTotalScore();
 		else
 			score = getTestScore();
 		if(Float.isNaN(score))
 			throw new CourseException("尚未设置"+(fromTotalScoreOrTestScore?"期末总评成绩":"结课考核成绩"));
 		return getGradePoint(score);
 	}
 	/**
 	 * 取得绩点。根据期末总评成绩计算
 	 * @return 期末总评成绩的绩点。0-59分的绩点为0
 	 * @throws CourseException 当  尚未设置期末总评成绩  时
 	 */
 	public float getGradePoint() throws CourseException{
 		return getGradePoint(true);
 	}
 	
 	/**
 	 * @return 课程性质
 	 */
 	public String getKind() {
 		return kind;
 	}
 	/**
 	 * @param kind 课程性质
 	 */
 	public Course setKind(String kind) {
 		this.kind = kind;
 		return this;
 	}
 	
 	/**
 	 * 增加教师，可以一次添加多名，用[,，;；]分开
 	 * @param teacher 教师名，可以是多名教师，用[,，;；]分开。如果为null，则直接退出
 	 * @return 参数合法返回this（builder），参数非法抛出异常
 	 */
 	public Course addTeacher(String teacher){
 		if(teacher == null)
 			return this;
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
 	 * @throws TimeAndAddressException 请见{@link TimeAndAddress#TimeAndAddress(String, String, String, String)}
 	 * @throws BitOperateException 请见{@link TimeAndAddress#TimeAndAddress(String, String, String, String)}
 	 * @see TimeAndAddress#TimeAndAddress(String, String, String, String)
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
 	 * @throws TimeAndAddressException 请见{@link TimeAndAddress#TimeAndAddress(String, Boolean, String, String, String)}
 	 * @throws BitOperateException 请见{@link TimeAndAddress#TimeAndAddress(String, Boolean, String, String, String)}
 	 * @see TimeAndAddress#TimeAndAddress(String, Boolean, String, String, String)
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
 	 * @throws NullPointerException 当timeAndAddress==null时
 	 */
 	public Course addTimeAndAddress(TimeAndAddress timeAndAddress) throws NullPointerException{
 		if(timeAndAddress == null)
 			throw new NullPointerException("timeAndAddress shouldn't have been null.");
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
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	@Override
 	public Course clone() throws CloneNotSupportedException {
 		Course clone = (Course) super.clone();
 		clone.setTeachers(this.teachers);
 		clone.setTimeAndAddresse(this.timeAndAddress);
 		return clone;
 	}
 
 	public static class CourseException extends Exception{
 		private static final long serialVersionUID = -4494349664328514829L;
 		public CourseException(String message){
 			super(message);
 		}
 		public CourseException(){
 			super("Encounter exception in Course class");
 		}
 		public CourseException(String message, Throwable cause) {
 			super(message, cause);
 		}
 		public CourseException(Throwable cause) {
 			super("Encounter exception in Course class", cause);
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
 		 * @throws TimeAndAddressException daysOfWeek 连续遇到[至到] 或者 [至到]的位置不对 或者 星期字符串不合语法，不含[一~六]或"日"
 		 * @throws BitOperateException 当 weeks非法（非以上格式，无法解析，或者超出0-20的范围） <br />
 		 * 或者  daysOfWeek中有不可识别的符号[^\\s\u00a0\u3000;；,，星期周日一二三四五六至到] ，超过0~7的范围 ，start>end <br />
 		 * 或者 periods不符合以上格式，无法解析 ， 数字超过1~13的范围  时
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
 		 * @throws TimeAndAddressException daysOfWeek 连续遇到[至到] 或者 [至到]的位置不对 或者 星期字符串不合语法，不含[一~六]或"日"
 		 * @throws BitOperateException 当 weeks非法（非以上格式，无法解析，或者超出0-20的范围） <br />
 		 * 或者  daysOfWeek中有不可识别的符号[^\\s\u00a0\u3000;；,，星期周日一二三四五六至到] ，超过0~7的范围 ，start>end <br />
 		 * 或者 periods不符合以上格式，无法解析 ， 数字超过1~13的范围  时
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
 		 * @throws BitOperateException 当 week中1超出0-20位的范围 或者  week中1超出0-20位的范围 或者 period中的1超出1~13位的范围 时
 		 */
 		public TimeAndAddress(int weekNumber,int dayOfWeek,int period,String address) 
 				throws BitOperateException{
 			//this();	//有没有必要呢
 			setWeek(weekNumber).setDay(dayOfWeek).setPeriod(period).setAddress(address);
 		}
 		/**
 		 * @param address 和时间对应的上课地点
 		 * @throws NullPointerException when address is null
 		 */
 		public TimeAndAddress(String address) throws NullPointerException{
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
 		 * 取得各个周是否有课。如果有课，返回值相应的元素为true；否则为false。0-20位置分别表示第0-20周。
 		 * 例如第1、4周有课，则result[1]、result[4]为true，其它为false
 		 * @return 各个周的有课情况。长度为21
 		 */
 		public boolean[] getWeekByBooleanArray(){
 			try {
 				return BitOperate.is1onEachBit(week, 20);
 			} catch (BitOperateException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 		/**
 		 * 类似setDay，0-20位的1/0表示是否有这一周。如16进制0x00 00 01 02 表示8、1周有课 
 		 * @param week the week to set
 		 * @return this（builder）
 		 * @throws BitOperateException 当 week中1超出0-20位的范围
 		 */
 		public TimeAndAddress setWeek(int week) throws BitOperateException {
 			if(BitOperate.has1onBitsHigherThan(week, 21))
 				throw new BitOperateException("Too large week number.(>=21)", 
 						BitOperateException.TOO_LARGE_POSITON);
 			this.week = week;
 			return this;
 		}
 		/**
 		 * 设置周次。若参数中weeks[a]==true，则第a周置为有课。weeks.length应小于等于21
 		 * @param weeks 第0-20个元素分别表示0-20周。true的位置设为有课
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 weeks[≥21]==true或weeks.length太大
 		 */
 		public TimeAndAddress setWeek(boolean[] weeks) throws BitOperateException{
 			int save = this.week;
 			this.week = 0;
 			try {
 				addWeeks(weeks);
 			} catch (BitOperateException e) {
 				this.week = save;
 				throw e;
 			}
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
 		 * 取得各个星期是否有课。如果有课，返回值相应的元素为true；否则为false。0-6位置分别表示周日到周六。
 		 * 例如第星期日、星期四有课，则result[0]、result[4]为true，其它为false
 		 * @return 各个星期的有课情况。长度为7，最大到result[6]（周六）
 		 */
 		public boolean[] getDayByBooleanArray(){
 			try {
 				return BitOperate.is1onEachBit(day, 6);
 			} catch (BitOperateException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 		/**
 		 * 参数的0-6位分别表示周日到周六，（第7位也表示周日，但用0更好）.1表有这一天，0表无。如二进制0010 0010表示星期一、星期五
 		 * @param daysOfWeek 一个整数，低到高位非别表示周日，周一，周二到周六。各位中1表真，0表假
 		 * @throws BitOperateException 当 dayOfWeek中的1超出0~7位的范围
 		 */
 		public TimeAndAddress setDay(int daysOfWeek) throws BitOperateException {
 			if(BitOperate.has1onBitsHigherThan(daysOfWeek, 8))
 				throw new BitOperateException("Too large day of week.(>=8)", 
 						BitOperateException.TOO_LARGE_POSITON);
 			if(BitOperate.is1onCertainBit(daysOfWeek, 7)){
 				daysOfWeek = BitOperate.add1onCertainBit(daysOfWeek, 0);
 				daysOfWeek = BitOperate.add0onCertainBit(daysOfWeek, 7);
 			}
 			this.day = (byte)daysOfWeek;
 			return this;
 		}
 		/**
 		 * 设置星期。例如参数中daysOfWeek[0]==true或daysOfWeek[7]==true，则星期日置为有课。
 		 * daysOfWeek.length应小于等于8
 		 * @param daysOfWeek 0、7位置表示周日，1-6表周一到周六。true表示相应星期应置为有课
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 dayOfWeek[≥8]==true或daysOfWeek.length太大
 		 */
 		public TimeAndAddress setDay(boolean[] daysOfWeek) throws BitOperateException{
 			byte save = this.day;
 			this.day = 0;
 			try {
 				addDays(daysOfWeek);
 			} catch (BitOperateException e) {
 				this.day = save;
 				throw e;
 			}
 			return this;
 		}
 		/**
 		 * 类似getDay，1-13位表1-13节课
 		 * @return the period
 		 */
 		public short getPeriod() {
 			return period;
 		}
 		/**
 		 * 取得各个节次是否有课。如果有课，返回值相应的元素为true；否则为false。1-13位置分别表示1-13节课。
 		 * 例如第1、4节有课，则result[1]、result[4]为true，其它为false。<br />
 		 * <strong>注</strong>：result[0]永远为false
 		 * @return 各个节次的有课情况。长度为14，result[0]===false
 		 */
 		public boolean[] getPeriodByBooleanArray(){
 			try {
 				return BitOperate.is1onEachBit(period, 13);
 			} catch (BitOperateException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 		/**
 		 * 类似setDay，1-13为表1-13节课
 		 * @param period the period to set
 		 * @throws BitOperateException 当 period中的1超出1~13位的范围
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
 		 * 设置课时（第几节课）。若参数中periods[a]==true，则第a节课置为有课。
 		 * weeks.length应小于等于14且periods[0]≠true
 		 * @param periods 1-13元素分别表第1到13节课。true表示相应节次应置为有课
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 periods[≥14]==true 或者 periods[0]==true 或者 periods.length太大
 		 */
 		public TimeAndAddress setPeriod(boolean[] periods) throws BitOperateException{
 			short save = this.period;
 			this.period = 0;
 			try {
 				addPeriods(periods);
 			} catch (BitOperateException e) {
 				this.period = save;
 				throw e;
 			}
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
 		 * @throws NullPointerException when address is null
 		 */
 		public TimeAndAddress setAddress(String address) throws NullPointerException {
 			if(address == null)
 				throw new NullPointerException("Shouldn't set address to null.");
 			this.address = address;
 			return this;
 		}
 
 		/**
 		 * 增加周次，识别类似2,4,6,8,10,12,14,18 01-20的字符串<br />
 		 * <strong>注意：</strong>2,4,6,8,10,12,14,18 01- 20（注意最后一个空格）无效
 		 * @param weeksStr 类似2,4,6,8,10,12,14,18 01-21的字符串
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 参数非法（非以上格式，无法解析，或者超出0-20的范围）时
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
 		 * @throws BitOperateException 当 参数非法（非以上格式，无法解析，或者超出0-20的范围）时
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
 		 * @throws BitOperateException 当 start或end超出0~20的范围，或start>end 时
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
 		 * @throws BitOperateException 当 start或end超出0~20的范围，或start>end 时
 		 */
 		public TimeAndAddress addWeeksOnRang(int start, int end) throws BitOperateException{
 			return addWeeksOnRange(start, end, null);
 		}
 		/**
 		 * 增加周次
 		 * @param week 0-20分别表示0-20周
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 week超出0-20的范围
 		 */
 		public TimeAndAddress addWeek(int week) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.week, week);
 			return setWeek(result);
 		}
 		/**
 		 * 批量增加周次
 		 * @param weeks 0-20分别表示0-20周
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 week超出0-20的范围
 		 */
 		public TimeAndAddress addWeeks(int[] weeks) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.week, weeks);
 			return setWeek(result);
 		}
 		/**
 		 * 批量增加周次。若参数中weeks[a]==true，则第a周置为有课。weeks.length应小于等于21
 		 * @param weeks 第0-20个元素分别表示0-20周。true的位置设为有课
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 weeks[≥21]==true或weeks.length太大
 		 */
 		public TimeAndAddress addWeeks(boolean[] weeks) throws BitOperateException{
 			int result = BitOperate.add1onSomeBits(this.week, weeks);
 			return setWeek(result);
 		}
 		/**
 		 * 去掉第week周
 		 * @param week 0-20分别表示0-20周
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 week超出0-20的范围
 		 */
 		public TimeAndAddress removeWeek(int week) throws BitOperateException{
 			if(week > 20)
 				throw new BitOperateException("Too large week number.(>=21)", 
 						BitOperateException.TOO_LARGE_POSITON);
 			int result = BitOperate.add0onCertainBit(this.week, week);
 			return setWeek(result);
 		}
 		/**
 		 * 检查是否设置了某周，已设置返回true
 		 * @param testedWeek 被检测的周数
 		 * @return 若设置了此周返回true，没有设置返回false
 		 * @throws BitOperateException 当 testedWeek超出0~20的范围
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
 		 * @throws BitOperateException daysStr中有不可识别的符号[^\\s\u00a0\u3000;；,，星期周日一二三四五六至到] 或者 超过0~7的范围 或者 start>end
 		 * @throws TimeAndAddressException 连续遇到[至到] 或者 [至到]的位置不对 或者 星期字符串不合语法，不含[一~六]或"日"
 
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
 		/**
 		 * @param complexDaysStr
 		 * @return
 		 * @throws TimeAndAddressException 连续遇到[至到] 或者 [至到]的位置不对 或者 星期字符串不合语法，不含[一~六]或"日"
 		 * @throws BitOperateException 当超出0~31的范围，或时间start>end 时
 		 */
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
 		/**
 		 * 把中文周几或星期几转化为数字0~6，0表周日（星期日）
 		 * @param weekStr 要被转化的字符串
 		 * @return 0~6，0表周日（星期日），1~6表周一~周六（星期一~星期六）
 		 * @throws TimeAndAddressException weekStr不合法，不含[一~六]或"日"
 		 * @throws NullPointerException 当weekStr为null时
 		 */
 		public static int dayStringToNumber(String weekStr) throws TimeAndAddressException{
 			if(weekStr == null)
 				throw new NullPointerException("weekStr is NULL.");
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
 		 * @throws BitOperateException 超出0~7位的范围 或者 start>end 时
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
 		 * @throws BitOperateException 超出0~7位的范围 或者 start>end 时
 		 */
 		public TimeAndAddress addDaysOnRang(int start, int end) throws BitOperateException{
 			return addDaysOnRange(start, end, null);
 		}
 		/**
 		 * 增加星期
 		 * @param dayOfWeek 0 （推荐）、7表周日，1-6表周一到周六
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 dayOfWeek超出0~7的范围
 		 */
 		public TimeAndAddress addDay(int dayOfWeek) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.day, dayOfWeek);
 			return setDay(result);
 		}
 		/**
 		 * 批量增加星期
 		 * @param daysOfWeek 0、7表周日，1-6表周一到周六
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 dayOfWeek超出0~7的范围
 		 */
 		public TimeAndAddress addDays(int[] daysOfWeek) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.day, daysOfWeek);
 			return setDay(result);
 		}
 		/**
 		 * 批量增加星期。例如参数中daysOfWeek[0]==true或daysOfWeek[7]==true，则星期日置为有课。
 		 * daysOfWeek.length应小于等于8
 		 * @param daysOfWeek 0、7位置表示周日，1-6表周一到周六。true表示相应星期应置为有课
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 dayOfWeek[≥8]==true或daysOfWeek.length太大
 		 */
 		public TimeAndAddress addDays(boolean[] daysOfWeek) throws BitOperateException{
 			int result = BitOperate.add1onSomeBits(this.day, daysOfWeek);
 			return setDay(result);
 		}
 		/**
 		 * 去掉星期，即取消dayOfWeek那天
 		 * @param dayOfWeek 0 （推荐）、7表周日，1-6表周一到周六
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 dayOfWeek超出0~7的范围
 		 */
 		public TimeAndAddress removeDay(int dayOfWeek) throws BitOperateException{
 			if(dayOfWeek >= 8)
 				throw new BitOperateException("Too large day of week.(>=8)", 
 						BitOperateException.TOO_LARGE_POSITON);
 			if(dayOfWeek == 7)
 				dayOfWeek = 0;
 			int result = BitOperate.add0onCertainBit(this.day, dayOfWeek);
 			return setDay(result);
 		}
 		/**
 		 * 检查是否设置了某星期(day of week),，已设置返回true
 		 * @param testedDayOfWeek 被检查的星期数，0、7都表示周日，1-6分别表示周一~周六
 		 * @return 若设置了此星期返回true，没有设置返回false
 		 * @throws BitOperateException 当testedWeek超出0~7的范围
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
 		 * @throws BitOperateException 当不符合以上格式，无法解析 或者 数字超过1~13的范围 时
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
 		 * @throws TimeAndAddressException 当 bigPeriod超出1~6的范围 时
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
 		 * @throws BitOperateException 当 start、end超出1~13的范围 或者 start>end 时
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
 		 * @throws BitOperateException 当 start、end超出1~13的范围 或者 start>end 时
 		 */
 		public TimeAndAddress addPeriodsOnRang(int start, int end) throws BitOperateException{
 			return addPeriodsOnRange(start, end, null);
 		}
 		/**
 		 * 增加课时（第几节课）
 		 * @param period 1-13分别表第1到13节课
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 period超出1~13的范围 时
 		 */
 		public TimeAndAddress addPeriod(int period) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(this.period, period);
 			return setPeriod(result);
 		}
 		/**
 		 * 批量增加课时（第几节课）
 		 * @param periods 1-13分别表第1到13节课
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 period超出1~13的范围 时
 		 */
 		public TimeAndAddress addPeriods(int[] periods) throws BitOperateException{
 			int result = BitOperate.add1onCertainBit(period, periods);
 			return setPeriod(result);
 		}
 		/**
 		 * 批量增加课时（第几节课）。若参数中periods[a]==true，则第a节课置为有课。
 		 * weeks.length应小于等于14且periods[0]≠true
 		 * @param periods 1-13元素分别表第1到13节课。true表示相应节次应置为有课
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 periods[≥14]==true 或者 periods[0]==true 或者 periods.length太大
 		 */
 		public TimeAndAddress addPeriods(boolean[] periods) throws BitOperateException{
 			int result = BitOperate.add1onSomeBits(period, periods);
 			return setPeriod(result);
 		}
 		/**
 		 * 去掉课时（第几节课），即取消第period节课
 		 * @param period 1-13分别表第1到13节课
 		 * @return 参数合法返回this（builder），参数非法抛出异常
 		 * @throws BitOperateException 当 period超出1~13的范围 时
 		 */
		public TimeAndAddress removePeriod(int period) throws BitOperateException{
 			if(period > 13)
 				throw new BitOperateException("Too large period number.(>13)", 
 						BitOperateException.TOO_LARGE_POSITON);
 			if(period == 0)
 				throw new BitOperateException("Illegal period: 0.", 
 						BitOperateException.ILLEGAL_POSITION);
 			int result = BitOperate.add0onCertainBit(this.period, period);
 			return setPeriod(result);
 		}
 		/**
 		 * 检查是否设置了某节课，已设置返回true
 		 * @param testedPeriod 被检测的周数，1-13分别表示1-13节课
 		 * @return 若设置了此周返回true，没有设置返回false
 		 * @throws BitOperateException 当 testedWeek超出1~13的范围 时
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
 		/**处理TimeAndAddress，遇到不可处理情况时的异常*/
 		public static class TimeAndAddressException extends Exception{
 			private static final long serialVersionUID = 1900908778609873214L;
 			public TimeAndAddressException(String message){
 				super(message);
 			}
 			public TimeAndAddressException(){
 				super("Encounter Exception in class TimeAndAddress.");
 			}
 			public TimeAndAddressException(String message, Throwable cause) {
 				super(message, cause);
 			}
 			public TimeAndAddressException(Throwable cause) {
 				super("Encounter Exception in class TimeAndAddress.", cause);
 			}
 		}
 	}
 }

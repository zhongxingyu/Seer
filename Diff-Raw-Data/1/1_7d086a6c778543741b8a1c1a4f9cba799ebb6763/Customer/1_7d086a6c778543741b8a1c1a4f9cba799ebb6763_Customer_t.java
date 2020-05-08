 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Customer {
 
 	private Date birthDate;
 	private String nickName;
 
 	public Customer(String nickName, String birthDate) throws Exception {
 		validateCustomerIfno(nickName, birthDate);

 		this.nickName = nickName;
 		this.birthDate = parseStirngToDateTime(birthDate);
 
 	}
 
 	private void validateCustomerIfno(String nickName, String birthDate) throws IllegalCustomerInfoException {
 		if (!isTheNickNameConsistedOfLowercaseAnddigitalOnly(nickName)) {
 			throw new IllegalCustomerInfoException("The nicke name must be consisted of lowercase and digital");
 		}
 		if (!isTheBirthDateFormatIsRight(birthDate)) {
 			throw new IllegalCustomerInfoException("The birth date should be like xxxx-xx-xx");
 		}
 	}
 
 	private Date parseStirngToDateTime(String birthDate) throws ParseException {
 		return getSimpleDateFormat().parse(birthDate);
 	}
 
 	private SimpleDateFormat getSimpleDateFormat() {
 		return new SimpleDateFormat( "yyyy-MM-dd");
 	}
 
 	private boolean isTheNickNameConsistedOfLowercaseAnddigitalOnly(String nickName) {
 		Pattern pattern = Pattern.compile("[a-z0-9]+");
 		Matcher matcher = pattern.matcher(nickName);
 		return matcher.matches();
 	}
 
 	public String getNickName() {
 		return nickName;
 	}
 
 	public String getBirthDate() throws ParseException {
 		return getSimpleDateFormat().format(birthDate);
 	}
 
 	public boolean isTheBirthDateFormatIsRight(String birthDate) {
 		Pattern pattern = Pattern.compile("\\d{4}\\-(0?[1-9]|[1][012])\\-(0?[1-9]|[12][0-9]|3[01])");
 		Matcher matcher = pattern.matcher(birthDate);
 		return matcher.matches();
 	}
 }

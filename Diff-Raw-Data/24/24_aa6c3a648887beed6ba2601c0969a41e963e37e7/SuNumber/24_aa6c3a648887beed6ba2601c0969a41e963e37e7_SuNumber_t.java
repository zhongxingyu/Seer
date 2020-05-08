 package models;
 
 import java.util.List;
 
 import com.google.common.collect.Lists;
 
 public class SuNumber {
 	public List<RowNumber> rows;
 
 	public SuNumber() {
 	}
 
 	public SuNumber(RowNumber... nums) {
 		this.rows = Lists.newArrayList(nums);
 	}
 
 	public static class RowNumber {
 		public List<OneNumber> oneNumbers;
 
 		public RowNumber() {
 		}
 
 		public RowNumber(OneNumber... oneNumber) {
 			this.oneNumbers = Lists.newArrayList(oneNumber);
 		}
 	}
 
 	public static class OneNumber {
 		public String num;
 		public String disabled;
		public String fixed;
		
 		public OneNumber() {
 		}
 
 		public OneNumber(String num) {
 			this.num = num;
 			empty();
 		}
 
 		public String empty() {
 			boolean equals = num.equals("");
 			if (equals) {
 				disabled = "";
 			} else {
 				disabled = "readonly";
 			}
 			return disabled;
 		}
 
 		public void nullToEmpty() {
 			if (num == null) {
 				num = "";
 			}
 			empty();
 		}
 	}
 
 	public void out() {
 		for (RowNumber row : rows) {
 			List<OneNumber> oneNumver = row.oneNumbers;
 			for (OneNumber oneNumber : oneNumver) {
 				System.out.println(oneNumber.num);
 			}
 		}
 	}
 
 	public void nullToEmpty() {
 		for (RowNumber row : rows) {
 			List<OneNumber> oneNumver = row.oneNumbers;
 			for (OneNumber oneNumber : oneNumver) {
 				oneNumber.nullToEmpty();
 			}
 		}
 	}
 }

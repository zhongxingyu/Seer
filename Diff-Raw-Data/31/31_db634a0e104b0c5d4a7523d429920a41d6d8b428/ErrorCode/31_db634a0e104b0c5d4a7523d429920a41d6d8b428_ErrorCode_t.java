 package ru.sabstest;
 
 import java.sql.Date;
 import java.util.Arrays;
 import java.util.Calendar;
 
 import javax.xml.crypto.KeySelector.Purpose;
 
 public class ErrorCode {
 	private static final String[] codes = {"39","49"};
 
 	public static boolean contains(String s)
 	{
 		return Arrays.asList(codes).contains(s);
 	}
 
 	public static void addError(PaymentDocument pd, int errorCode)
 	{
 		switch(errorCode)
 		{
 		case 12:
 			Calendar c = Calendar.getInstance(); 
 			c.setTime(pd.edDate); 
 			c.add(Calendar.DATE, 1);
 			pd.edDate =  new Date(c.getTimeInMillis());			
 			break;
 		case 13:
 			c = Calendar.getInstance(); 
 			c.setTime(pd.accDocDate); 
 			c.add(Calendar.DATE, 1);
 			pd.accDocDate =  new Date(c.getTimeInMillis());			
 			break;
 		case 15:
 			pd.priority="0";
 			break;
 		case 16:
 			c = Calendar.getInstance(); 
 			c.setTime(pd.chargeOffDate); 
 			c.add(Calendar.DATE, -1);
 			pd.chargeOffDate =  new Date(c.getTimeInMillis());		
 			break;
 		case 17://
 			pd.transKind = "09";
 			break;
 		case 18://
 			pd.payer.inn = "abc";
 			break;
 		case 19://
 			pd.payee.inn = "abc";
 			break;
 		case 20://
 			pd.payer.name = "abc";
 			break;
 		case 21://
 			pd.payee.name = "abc";
 			break;
 		case 22://
 			pd.purpose = "abc";
 			break;
 		case 23:
 //			pd.edAuthor = "132";
 			break;
 		case 31:
 			pd.payer.bic = "999999999";
 			break;
 		case 32:
 			pd.payer.correspAcc = "99999810300000000001";
 			break;
 		case 33:
 			pd.payer.correspAcc = "";
 			break;
 		case 34:
 			pd.payee.bic = "999999999";
 			break;
 		case 35:
 			pd.payee.correspAcc = "99999810300000000001";
 			break;
 		case 36:
 			pd.payee.correspAcc = "";
 			break;
		case 41:
			String cr = pd.payer.personalAcc.substring(8,9);
			int cd = Integer.parseInt(cr) + 1;
			cd = (cd == 10) ? 0 : cd;
			pd.payer.personalAcc = pd.payer.personalAcc.substring(0,8) + Integer.toString(cd) + pd.payer.personalAcc.substring(9);
			break;
 		case 42:
 		//	pd.payer.personalAcc = "a" + pd.payer.personalAcc.substring(1);
 			break;
 		case 43:
 			pd.payer.personalAcc = "55999"  + pd.payer.personalAcc.substring(5);
 			pd.payer.contrrazr();
 			break;
 		case 44:
 			pd.payer.personalAcc = "";
 			break;
 		case 45:
			cr = pd.payee.personalAcc.substring(8,9);
			cd = Integer.parseInt(cr) + 1;
 			cd = (cd == 10) ? 0 : cd;
 			pd.payee.personalAcc = pd.payee.personalAcc.substring(0,8) + Integer.toString(cd) + pd.payee.personalAcc.substring(9);
 			break;
 		case 46:
 		//	pd.payee.personalAcc = "a" + pd.payer.personalAcc.substring(1);
 			break;
 		case 47:
 			pd.payee.personalAcc = "55999"  + pd.payer.personalAcc.substring(5);
 			pd.payee.contrrazr();
 			break;
 		case 48:
 			pd.payee.personalAcc = "";
 			break;
 		case 49:
 			pd.payee.personalAcc = "40101810999999999999";
 			pd.payee.contrrazr();
 			break;
 		case 50:
 			break;
 		case 51:
 			pd.receiptDate = null;
 			break;
 		case 52:
 			break;
 		case 53:
 			pd.chargeOffDate = null;
 			break;
 		case 54:
 			pd.tax = new DepartmentalInfo("a", "18210302011011000110", "456", "8", "90", "12", "34", "56");
 			break;
 		case 55:
 			pd.tax = new DepartmentalInfo("1", "18210302011011000110", "456", "8", "90", "12", "34", "56");
 			pd.payer.kpp = "";
 			break;
 		case 56:
 			pd.tax = new DepartmentalInfo("1", "18210302011011000110", "456", "8", "90", "12", "34", "56");
 			pd.payee.kpp = "";
 			break;
 		case 57:
 			pd.tax = new DepartmentalInfo("1", "", "456", "8", "90", "12", "34", "56");
 			break;
 		case 58:
 			pd.tax = new DepartmentalInfo("1", "18210302011011000110", "", "8", "90", "12", "34", "56");
 			break;
 		case 59:
 			pd.tax = new DepartmentalInfo("1", "18210302011011000110", "456", "", "90", "12", "34", "56");
 			break;
 		case 60:
 			pd.tax = new DepartmentalInfo("1", "18210302011011000110", "456", "8", "", "12", "34", "56");
 			break;
 		case 61:
 			pd.tax = new DepartmentalInfo("1", "18210302011011000110", "456", "8", "90", "", "34", "56");
 			break;
 		case 62:
 			pd.tax = new DepartmentalInfo("1", "18210302011011000110", "456", "8", "90", "12", "", "56");
 			break;
 		case 63:
 			pd.tax = new DepartmentalInfo("1", "18210302011011000110", "456", "8", "90", "12", "34", "");
 			break;
 		case 64:
 			CollectionOrder co = (CollectionOrder) pd;
 			co.receiptDateCollectBank = null;
 			break;
 		case 65:
 			PaymentWarrant pw = (PaymentWarrant) pd;
 			pw.ppPaytNo = "0";
 			break;
 		case 66:
 			pw = (PaymentWarrant) pd;
 			pw.ppTransKind = "99";
 			break;
 		case 67:
 			pw = (PaymentWarrant) pd;
 			pw.ppAccDocNo = "1";
 			break;
 		case 68:
 			pw = (PaymentWarrant) pd;
 			pw.ppAccDocDate = new Date(0);
 			break;
 		case 69:
 			pw = (PaymentWarrant) pd;
 			pw.ppSumResidualPayt = 0;
 			break;
 		case 70:
 			pw = (PaymentWarrant) pd;
 			pw.transContent = "!@abc";
 			break;		
		case 71:
			co = (CollectionOrder) pd;
			co.paytKind = "5";
			break;
		case 72:
			PaymentOrderRegister por = (PaymentOrderRegister) pd;
			por.tiList.get(0).transactionSum += 1;
			break;
 		}
 	}
 }

 package storage;
 
 
 public class TestDriver {
 	public static void main(String[] args){
 		Accounts accounts = new Accounts();
 		accounts.addAccount(new Account("Admin", accounts.hashPass("admin"),Group.ADMINISTRATOR));
 		/*
 		accounts.addAccount(new Account("User", accounts.hashPass("qwerty"), Group.STANDARD));
 		accounts.addAccount(new Account("NotSoRandomAccountName", accounts.hashPass("z"), Group.POWER));
 		accounts.addAccount(new Account("RandomAccountName", accounts.hashPass("x"), Group.STANDARD));
 		accounts.addAccount(new Account("SuperRandomAccountName", accounts.hashPass("t"), Group.ADMINISTRATOR));
 		accounts.addAccount(new Account("AccountName", accounts.hashPass("u"), Group.POWER));
 		accounts.addAccount(new Account("User1", accounts.hashPass("g"),Group.STANDARD));
 		Account cd = new Account("cd",accounts.hashPass("qwerty"),Group.STANDARD);
 		cd.removePermission(Permission.DELETEFILTER);
 		cd.removePermission(Permission.CREATEFILTER);
 		cd.removePermission(Permission.EDITFILTER);
 		accounts.addAccount(cd);*/
 		try {
 			Account user = accounts.getAccount("Admin",accounts.hashPass("admin"));
			user.addFilter(new Filter("Remove Images", "(?i)(?s)<img(.)*?>", "<!-- Killed Image -->",false));
			user.addFilter(new Filter("Remove Scripts", "(?i)(?s)<script(.)*?</script>", "<!-- Killed Script -->",false));
			user.addFilter(new Filter("Remove Noscripts (use with Remove Scripts)","(?i)(?s)<noscript(.)*?</noscript>","<!-- Killed Noscript -->",false));
			user.addFilter(new Filter("Remove Objects", "(?i)(?s)<object(.)*?</object>", "<!-- Killed Object -->", false));
			user.addInactiveFilter(new Filter("Remove Applets", "(?i)(?s)<applet(.)*?</applet>", "<!-- Killed Applet -->",false));/*
 			user.addFilter(new Filter("No Images", "(?i)(?s)<img(.)*?>", "<!-- Killed Image -->"));
 			user.addFilter(new Filter("No Script", "(?i)(?s)<script(.)*?</script>", "<!-- Killed Script -->"));
 			user.addFilter(new Filter("No Nosccript","(?i)(?s)<noscript(.)*?</noscript>","<!-- Killed Noscript -->"));
 			user.addFilter(new Filter("No Objects", "(?i)(?s)<object(.)*?</object>", "<!-- Killed Object -->"));
 			user.addInactiveFilter(new Filter("No Applets", "(?i)(?s)<applet(.)*?</applet>", "<!-- Killed Applet -->"));*/
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		accounts.saveAccounts();
 		accounts.loadAccounts();
 		for(Account a: accounts){
 			System.out.println(a.toString());
 		}
 	}
 }

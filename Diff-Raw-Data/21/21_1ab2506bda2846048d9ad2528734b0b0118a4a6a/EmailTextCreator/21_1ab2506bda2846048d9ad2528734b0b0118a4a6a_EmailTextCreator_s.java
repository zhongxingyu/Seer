 package server.operations.email;
 
 import server.entities.Form;
 import server.entities.Login;
 import server.entities.Newsletter;
 import server.entities.Replacement;
 import server.operations.AccountHandler;
 import server.operations.NewsLetterHandler;
 
 /**
  * Class for generating the text for the schedule mails.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_25
  * @version 1.0
  */
 
 class EmailTextCreator {
 
 	String generateScheduleChangeText(final Replacement replacement) {
 
 		final StringBuilder sb = new StringBuilder();
 
 		sb.append("Hallo,");
 		sb.append(System.lineSeparator());
 		sb.append("hiermit weisen wir Sie auf eine Abweichung ihres regulären Stundenplans hin.");
 		sb.append(System.lineSeparator());
 
 		sb.append(System.lineSeparator());
 		sb.append("Betroffener Tag:");
		sb.append("Woche: "+replacement.getWeek());
		sb.append("Tag:"+replacement.getDay().getDescription());
		sb.append("Stunde: "+ replacement.getLesson().getTimeFrom() + " - " + replacement.getLesson().getTimeTo());
 		sb.append(System.lineSeparator());
 
		sb.append("Neuer Raum:");
 		sb.append(replacement.getRoom().getDescription()); // Raum name
 		sb.append(System.lineSeparator());
 
		sb.append("Fach:");
 		String fach = "-";
 		if (replacement.getSubject() != null) {
 			fach = replacement.getSubject().getDescription();
 		}
 
 		sb.append(fach);
 		sb.append(System.lineSeparator());
 		if (replacement.getCancel() == 1) {
 			sb.append("Änderung: Die Stunde Fällt aus!");
 
 		} else {
			sb.append("Vertretungslehrer:");
 			sb.append(replacement.getTeacher().getFirstname() + " " + replacement.getTeacher().getName());
 		}
 
 		sb.append(System.lineSeparator());
		sb.append("Notiz:");
 		String note = replacement.getNote();
 		if (note == null) {
 			note = "-";
 		}
 		sb.append(note);
 		sb.append(System.lineSeparator());
 
 		sb.append(System.lineSeparator());
 		sb.append("Hinweis: Hierbei handelt es sich um eine einmalige Änderung.");
 		sb.append(System.lineSeparator());
 		sb.append("Diese hat keinerlei Einfluss auf den regulären Stundenplan.");
 		sb.append(System.lineSeparator());
 		sb.append("Bitte beachten Sie, dass sich weitere Anpassungen ergeben könnten.");
 		sb.append(System.lineSeparator());
 		sb.append("Sie werden in diesem Fall erneut von uns benachrichtig.");
 		sb.append(System.lineSeparator());
 
 		sb.append(System.lineSeparator());
 		sb.append("Wir entschuldigen uns vielmals für die Unannehmlichkeiten.");
 		sb.append(System.lineSeparator());
 		sb.append("Ihr National Studenplan Agency Team.");
 
 		return sb.toString();
 	}
 
 	String generateConformationText(final Form form, final String eMailAddress) {
 
 		final StringBuilder sb = new StringBuilder();
 
 		sb.append("Hallo,");
 		sb.append(System.lineSeparator());
 		sb.append("sie haben sich für den Newsletter der Nation Stundenplan Agency registriert.");
 		sb.append(System.lineSeparator());
 		sb.append("Die für sie eingetragene Klasse ist die " + form.getDescription() + ".");
 		sb.append(System.lineSeparator());
 
 		sb.append(System.lineSeparator());
 		sb.append("Bitte bestätigen sie ihre Anmeldung indem Sie auf den folgenden Link klicken:");
 		sb.append(System.lineSeparator());
 		sb.append(new NewsLetterHandler().generateRegistrationLink(form, eMailAddress));
 		sb.append(System.lineSeparator());
 
 		sb.append(System.lineSeparator());
 		sb.append("Sollten Sie die Registrierung nicht durchgeführt haben oder keine Newsletter erhalten wollen,");
 		sb.append(System.lineSeparator());
 		sb.append("ignorieren diese Nachricht.");
 		sb.append(System.lineSeparator());
 		sb.append("Ihre Addresse wird dann in kürze wieder gelöscht.");
 		sb.append(System.lineSeparator());
 
 		sb.append(System.lineSeparator());
 		sb.append("Ihr National Studenplan Agency Team.");
 
 		return sb.toString();
 	}
 
 	String generateUserCreationText(final String userName, final String password) {
 
 		final StringBuilder sb = new StringBuilder();
 
 		sb.append("Hallo,");
 		sb.append(System.lineSeparator());
 		sb.append("sie wurden als Administrator der Nation Stundenplan Agency registriert.");
 		sb.append(System.lineSeparator());
 
 		sb.append("Ihr Login ist: ");
 		sb.append(userName);
 		sb.append(System.lineSeparator());
 
 		sb.append("Ihr Passwort ist: ");
 		sb.append(password);
 		sb.append(System.lineSeparator());
 
 		sb.append(System.lineSeparator());
 		sb.append("Ihr National Studenplan Agency Team.");
 
 		return sb.toString();
 	}
 
 	public String generatePasswordChangedText(final String userName, final String password) {
 		final StringBuilder sb = new StringBuilder();
 
 		sb.append("Hallo,");
 		sb.append(System.lineSeparator());
 		sb.append("ihr passwort wurde erfolgreich geändert.");
 		sb.append(System.lineSeparator());
 
 		sb.append("Ihr neues Passwort ist: ");
 		sb.append(password);
 		sb.append(System.lineSeparator());
 
 		sb.append(System.lineSeparator());
 		sb.append("Ihr National Studenplan Agency Team.");
 
 		return sb.toString();
 	}
 
 	public String generateResetPasswordText(final Login login) {
 
 		final StringBuilder sb = new StringBuilder();
 
 		sb.append("Hallo,");
 		sb.append(System.lineSeparator());
 		sb.append("um Ihr Passwort zurückzusetzen klicken Sie hier:");
 		sb.append(System.lineSeparator());
 
 		sb.append(new AccountHandler().generateResetPasswordLink(login.getId()));
 		sb.append(System.lineSeparator());
 
 		sb.append(System.lineSeparator());
 		sb.append("Ihr National Studenplan Agency Team.");
 
 		return sb.toString();
 	}
 
 	public String generateRemoveRegistrationText(final Newsletter newsletter) {
 
 		final StringBuilder sb = new StringBuilder();
 
 		sb.append("Hallo,");
 		sb.append(System.lineSeparator());
 		sb.append("um sich von dem Newsletter von der Klasse " + newsletter.getForm().getDescription() + " abzumelden klicken Sie folgenden Link:");
 		sb.append(System.lineSeparator());
 
 		sb.append(System.lineSeparator());
 		sb.append(new NewsLetterHandler().generateRemoveLink(newsletter));
 		sb.append(System.lineSeparator());
 
 		sb.append(System.lineSeparator());
 		sb.append("Ihr National Studenplan Agency Team.");
 
 		return sb.toString();
 	}
 }

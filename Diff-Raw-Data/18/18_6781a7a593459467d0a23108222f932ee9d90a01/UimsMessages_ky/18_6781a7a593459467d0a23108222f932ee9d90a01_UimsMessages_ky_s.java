 package kg.cloud.uims.i18n;
 
 public class UimsMessages_ky extends UimsMessages {
 
 	static final Object[][] contents_ky = {
 			{ OkKey, "Мейли" },
 			{ CancelKey, "Жок" },
 			// Application
 			{ AppTitle, "Университетти башкаруу системасы" },
 			{ Username, "Колдонуучу" },
 			{ Password, "Сыр сөз" },
 			{ Login, "Кир" },
 			{ LoginButton, "Кир" },
 
 			// Authenticated screen
 			{ LogoutButton, "Чык" },
 			{ LogInAsLabel, "Вы зашли как" },
 			{ CurYearLabel, "Жыл" },
 			{ CurSemesterLabel, "Семестр" },
 			{ CurWeekLabel, "Апта" },
 			{ CurExamLabel, "Сыйнак" },
			{ButtonTooltip, "Биякта сыр создуну алмаштыр!"},
 
 			// Navigation Tree Branches
 			{ TBEduProcess, "Окуу Процеси" },
 
 			// Navigation Tree Sub-branches
 
 			{ TSBMySubjects, "Сабактарым" },
 			{ TSBMySuccess, "Жетишкендиктерим" },
 			{ TSBMyTranscript, "Транскриптим" },
 
 			// Supervisor Branches
 			{ TBSupervisor, "Куратор" },
 			{ TSBRegistration, "Каттоо" },
 			{ TSBTranscript, "Транскрипт" },
 			{ TSBSuccessReport, "Жетишкендик" },
 			// Instructor Branches
 			{ TBInstructor, "Окутуучу" },
 			{ TSBAttendance, "Жоктоо" },
 			{ TSBExam, "Сынак" },
 			// Registration View
 			{ RegistrationHeader, "Регистрация" },
 			{ RegistrationStudentName, "Аты" },
 			{ RegistrationStudentSurname, "Фамилиясы" },
 			{ RegistrationStudentEduStatus, "Окуу Статусу" },
 			{ AccountingStatus, "Бухгалтерия" },
 
 			{ TableNotTakenSubjects, "Алынбаган сабактар" },
 			{ TableCurrentSubjects, "Кезектеги сабактар" },
 
 			{ SubjectName, "Сабактын Аты" },
 			{ SubjectCode, "Сабактын Коду" },
 			{ SubjectCredit, "Кредити" },
 			{ SubjectHour, "Сааты" },
 			{ Semester, "Семестр" },
 			{ StudyYear, "Жыл" },
 			{ DepartmentCode, "Бөлүмдүн Коду" },
 			{ DepartmentName, "Бөлүмдүн Аты" },
 			{ SubjectRegistrationStatus, "Катталуу Статусу" },
 			{ SubjectHoursSum, "Саатардын Саны" },
 
 			{ MoveUpButton, "Ылдый жылдыр" },
 			{ MoveDownButton, "Ойдо жылдыр" },
 			{ SaveButton, "Сактоо" },
 
			{ NotifSumOFSubjExceed,
					"Сумма часов предметов не должна превышать 40!" },
			{ NotifNothingSelected, "Ничего не выбрано" },
			{ NotifCantRemoveSubj, "Вы не можете удалить этот предмет!" },
			{ NotifThankYouRegistr, "Спасибо, регистрацияуспешно завершена!" },
 
 			// Transcript View
 			{ TranscriptHeader, "Транскрипт" },
 			{ SubjectAverage, "Ортоломо баасы" },
 			{ SubjectCreditSum, "Кредиттин Саны" },
 			{ LabelStudent, "Студент" },
			{ ButtonMakePDF, "PDF жасоо" },
 
 			// SuccessReportView
 			{ LabelYear, "Академик жылы" }, { LabelSemester, "Семестр" },
 			{ SuccessReportHeader, "Жетишкендиктер" },
 			{ StudentMidterm, "Мидтерм" }, { StudentFinal, "Финал" },
 			{ StudentMakeup, "Мейкап" }, { StudentAttandance, "Жоктоо" },
 			// HelpView
 			{ HelpHeader, "Жардам" },
 			// ChangeUserDataView
			{ ChangeUserDataHeader, "Информациясы алмаштыр" } };
 
 	@Override
 	public Object[][] getContents() {
 		return contents_ky;
 	}
 
 }

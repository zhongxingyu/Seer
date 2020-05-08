 package kg.cloud.uims.i18n;
 
 public class UimsMessages_ky extends UimsMessages {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
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
 			{ LogInAsLabel, "Сиз" },
 			{ CurYearLabel, "Жыл" },
 			{ CurSemesterLabel, "Семестр" },
 			{ CurWeekLabel, "Апта" },
 			{ CurExamLabel, "Сынак" },
 			{ ButtonTooltip, "Биякта сыр сөздүнү алмаштыр!" },
 
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
 
			{ MoveUpButton, "Ойдо жылдыр" },
 			{ MoveDownButton, "Ылдый жылдыр" },
 			{ SaveButton, "Сактоо" },
 
 			{ NotifSumOFSubjExceed, "Саатардын суммасы 39 саатты ашпашы керек!" },
 			{ NotifNothingSelected, "Еч нерсе тандалган эмес." },
 			{ NotifCantRemoveSubj, "Сиз бул сабакты өчүрө албайсыз!" },
 			{ NotifThankYouRegistr, "Рахмат, Катоо ийгиликтүү аяктады!" },
 			{ FilterByYearLabel, "Жылга карап сүз:"},
 			{ FilterByCodeLabel, "Сабактын кодуна карап сүз:"},
 
 			// Transcript View
 			{ TranscriptHeader, "Транскрипт" },
 			{ SubjectAverage, "Ортоломо баасы" },
 			{ SubjectCreditSum, "Кредиттин Саны" },
 			{ LabelStudent, "Студент" },
 			{ ButtonMakePDF, "PDFке айлант " },
 
 			// SuccessReportView
 			{ LabelYear, "Академик жылы" }, { LabelSemester, "Семестр" },
 			{ SuccessReportHeader, "Жетишкендиктер" },
 			{ StudentMidterm, "Мидтерм" }, { StudentFinal, "Финал" },
 			{ StudentMakeup, "Мейкап" }, { StudentAttandance, "Жоктоо" },
 			
 			//AttendanceView
 			{AttendanceHeader, "Жоктоо"},
			{SubjectHoursPerWeek, "Часы в неделю"},
			{GroupName, "Название группы"},
			{TotalStudents, "Количество студентов"},
 			
 			// AttendanceWindow
 						{ButtonMakeForm, "Жоктоо формасы"},
						{ButtonMakeReport, "Жоокто репорту"},
						{NotifSuccesSave, "Успеваемость успешно сохранена"},
						{NotifDbError, "Ошибка! Вы уже добавили успеваемость"},
						{NotifNegative, "Числа не могут быть негативными"},
						{NotifWrongNum, "Неправильное значение! Пожалуйста, проверьте поля"},
 			
 			// HelpView
 			{ HelpHeader, "Жардам" },
 			// ChangeUserDataView
 			{ ChangeUserDataHeader, "Маалымат алмаштыр" },
 			{ FormCaptionUser, "Пользователь" },
 			{
 					FormDescription,
 					"Введите сложный пароль длинною 6-20 характеров - "
 							+ "цифры, латинские буквы и специальные символы." },
 			{ FormFiledCurPassword, "Текущий Пароль" },
 			{ FormFiledNewPassword, "Новый Пароль" },
 			{ FormFiledConfPassword, "Подтвердите Пароль" },
 
 			{ RequiredErrorCurrPassword, "Пожалуйста, введите Текущий Пароль!" },
 			{ RequiredErrorNewPassword, "Пожалуйста, введите Новый Пароль!" },
 			{ RequiredErrorConfPassword,
 					"Пожалуйста, подтвердите Новый Пароль!" },
 			{
 					RegexpValidatorError,
 					"Пароль должен содержать не менее 6 характеров "
 							+ "и состоять из допустимых символов" },
 			{
 					NotifDontMatch,
 					"Новый Пароль и Пароль подтверждения не совпадают,"
 							+ " пожалуйста, подтвердите их!" },
 			{ NotifWrongCurrPassword,
 					"Пожалуйста, введите текущий пароль правильно!" },
 			{ NotifSuccessfulChange, "Пароль успешно изменен." },
 			{FreezedMessage,"Тоңдурулган! Бул окуучу Бухгалтерияга баруусу зарыл!"}
 	};
 
 	@Override
 	public Object[][] getContents() {
 		return contents_ky;
 	}
 
 }

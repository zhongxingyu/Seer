 package cl.votainteligente.inspector.client.i18n;
 
 import com.google.gwt.i18n.client.Messages;
 
 public interface ApplicationMessages extends Messages {
 
 	@Key("general.bill")
 	public String getGeneralBill();
 
 	@Key("general.category")
 	public String getGeneralCategory();
 
 	@Key("general.chamber")
 	public String getGeneralChamber();
 
 	@Key("general.district")
 	public String getGeneralDistrict();
 
 	@Key("general.initiativeType")
 	public String getGeneralInitiativeType();
 
 	@Key("general.parlamentarian")
 	public String getGeneralParlamentarian();
 
 	@Key("general.parliamentariansInConflict")
 	public String getGeneralParlamentariansInConflict();
 
 	@Key("general.party")
 	public String getGeneralParty();
 
 	@Key("general.society")
 	public String getGeneralSociety();
 
 	@Key("general.societiesInConflict")
 	public String getGeneralSocietiesInConflict();
 
 	@Key("general.stage")
 	public String getGeneralStage();
 
 	@Key("general.subscribe")
 	public String getGeneralSubscribe();
 
 	@Key("general.profile")
 	public String getGeneralProfile();
 
 	@Key("general.viewMore")
 	public String getGeneralViewMore();
 
 	@Key("general.title")
 	public String getGeneralTitle();
 
 	@Key("general.type")
 	public String getGeneralType();
 
 	@Key("general.urgency")
 	public String getGeneralUrgency();
 
 	@Key("general.by")
 	public String getGeneralBy();
 
 	@Key("general.noMatches")
 	public String getGeneralNoMatches();
 
 	@Key("error.incorrectParameters")
 	public String getErrorIncorrectParameters();
 
 	@Key("error.invalidEmail")
 	public String getErrorInvalidEmail();
 
 	@Key("error.parlamentarian")
 	public String getErrorParlamentarian();
 
 	@Key("error.parlamentarian.list")
 	public String getErrorParlamentarianList();
 
 	@Key("error.parlamentarian.search")
 	public String getErrorParlamentarianSearch();
 
 	@Key("error.parlamentarian.billSearch")
 	public String getErrorParlamentarianBillSearch();
 
 	@Key("error.parlamentarian.categorySearch")
 	public String getErrorParlamentarianCategorySearch();
 
 	@Key("error.category")
 	public String getErrorCategory();
 
 	@Key("error.category.list")
 	public String getErrorCategoryList();
 
 	@Key("error.category.search")
 	public String getErrorCategorySearch();
 
 	@Key("error.category.parlamentarianSearch")
 	public String getErrorCategoryParlamentarianSearch();
 
 	@Key("error.society")
 	public String getErrorSociety();
 
 	@Key("error.society.list")
 	public String getErrorSocietyList();
 
 	@Key("error.bill")
 	public String getErrorBill();
 
 	@Key("error.bill.list")
 	public String getErrorBillList();
 
	@Key("error.bill.authors")
	public String getErrorBillAuthors();

 	@Key("error.subscriber")
 	public String getErrorSubscriber();
 
 	@Key("error.subsriber.load")
 	public String getErrorSubscriberLoad();
 
 	@Key("error.subscriber.key")
 	public String getErrorSubscriberKey();
 
 	@Key("error.subscriber.unsubscribe")
 	public String getErrorSubscriberUnsubscribe();
 
 	@Key("bill.bulletin")
 	public String getBillBulletin();
 
 	@Key("bill.entryDate")
 	public String getBillEntryDate();
 
 	@Key("bill.originChamber")
 	public String getBillOriginChamber();
 
 	@Key("bill.conflictedBills")
 	public String getBillConflictedBill();
 
 	@Key("bill.isAuthoredBill")
 	public String getBillIsAuthoredBill();
 
 	@Key("bill.votedInChamber")
 	public String getBillVotedInChamber();
 
 	@Key("category.searchMessage")
 	public String getCategorySearchMessage();
 
 	@Key("category.notificationSelectParliamentarian")
 	public String getCategoryNotificationSelectParliamentarian();
 
 	@Key("parlamentarian.societies")
 	public String getParlamentarianSocieties();
 
 	@Key("parlamentarian.authoredBills")
 	public String getParlamentarianAuthoredBills();
 
 	@Key("parlamentarian.votedBills")
 	public String getParlamentarianVotedBills();
 
 	@Key("parlamentarian.interestDeclarationFile")
 	public String getParlamentarianInterestDeclarationFile();
 
 	@Key("parlamentarian.patrimonyDeclarationFile")
 	public String getParlamentarianPatrimonyDeclarationFile();
 
 	@Key("parlamentarian.searchMessage")
 	public String getParlamentarianSearchMessage();
 
 	@Key("parliamentarian.notificationSelectCategory")
 	public String getParliamentarianNotificationSelectCategory();
 
 	@Key("person.civilStatusSingle")
 	public String getCivilStatusSingle();
 
 	@Key("person.civilStatusMarried")
 	public String getCivilStatusMarried();
 
 	@Key("person.civilStatusSeparated")
 	public String getCivilStatusSeparated();
 
 	@Key("person.civilStatusDivorced")
 	public String getCivilStatusDivorced();
 
 	@Key("person.civilStatusWidowed")
 	public String getCivilStatusWidowed();
 
 	@Key("society.reported")
 	public String getSocietyReported();
 
 	@Key("society.unreported")
 	public String getSocietyUnreported();
 
 	@Key("society.areaOfInterest")
 	public String getSocietyAreaOfInterest();
 
 	@Key("society.legalName")
 	public String getSocietyLegalName();
 
 	@Key("society.reportedThis")
 	public String getSocietyReportedThis();
 
 	@Key("society.isInConflict")
 	public String getSocietyIsInConflict();
 
 	@Key("society.viewMore")
 	public String getSocietyViewMore();
 
 	@Key("society.consistencyIndex")
 	public String getSocietyConsistencyIndex();
 
 	@Key("society.reportedVsUnreported")
 	public String getSocietyReportedVsUnreported();
 
 	@Key("subscriber.subscribe")
 	public String getSubscriberSuscribe();
 
 	@Key("subscriber.subscriptionSuccessful")
 	public String getSubscriberSuscriptionSuccessful();
 
 	@Key("subscriber.unsubscribe.message")
 	public String getSubscriberUnsubscribeMessage(String type, String content);
 
 	@Key("subscriber.unsubscribe.all")
 	public String getSubscriberUnsubscribeAll();
 
 	@Key("subscriber.unsubscribeSuccessful")
 	public String getSubscriberUnsubscribeSuccesful();
 }

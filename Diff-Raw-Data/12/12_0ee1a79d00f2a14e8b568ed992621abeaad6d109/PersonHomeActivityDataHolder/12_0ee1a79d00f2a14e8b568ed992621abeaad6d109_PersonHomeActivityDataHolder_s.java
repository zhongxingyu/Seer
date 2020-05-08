 package ca.charland.tenideas.manage;
 
import ca.charland.activity.DateAndTimeActivity;
 import ca.charland.activity.manage.BasePersonHomeActivityDataHolder;
 
 public class PersonHomeActivityDataHolder extends BasePersonHomeActivityDataHolder {
 
 	@Override
 	public Class<?> getNextAddClass() {
		return DateAndTimeActivity.class;
 	}
 
 	@Override
 	public Class<?> getNextViewClass() {
 		return DateListOfPreviousEntriesActivity.class;
 	}
 
 	@Override
 	protected Class<?> getNextClass() {
 		return AllPeopleListActivity.class;
 	}
 }

 package com.quizmania.utils;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.support.v4.app.Fragment;
 
 import com.quizmania.entities.QuizElement;
 
 public class QuizElementUtil {
 	public QuizElement element;
 
	public static int getResourceIdFromQuizElement(Context context, QuizElement element){				
 			Resources res = context.getResources();
 			return getResourceIdFromResourceName(res, element.getImageName());
 		}
 	
	public static int getResourceIdFromResourceName(Resources res, String resourceName){		
 		int resID = res.getIdentifier(resourceName, "drawable",
 				StaticGlobalVariables.packageName);
 		return resID;
 		
 	}
 	public static int getResourceIdFromQuizElement(Fragment context, QuizElement element){
 		Resources res = context.getResources();
 		return getResourceIdFromResourceName(res, element.getImageName());
 	}
 }

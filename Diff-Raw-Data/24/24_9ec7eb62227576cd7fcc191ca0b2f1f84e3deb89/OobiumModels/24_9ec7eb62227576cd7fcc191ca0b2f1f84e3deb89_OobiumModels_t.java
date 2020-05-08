 package org.oobium.guides.models.guides;
 
 import org.oobium.app.server.view.View;
 import org.oobium.guides.models.Chapter;
 import org.oobium.guides.models.Guide;
 import org.oobium.guides.views.guides.oobium_models.Intro;
 import org.oobium.guides.views.guides.oobium_models.model_object_lifecycle.CachingAndSessions;
 import org.oobium.guides.views.guides.oobium_models.model_object_lifecycle.CustomCode;
 import org.oobium.guides.views.guides.oobium_models.model_object_lifecycle.ModelObjectLifecycle;
 import org.oobium.guides.views.guides.oobium_models.model_object_lifecycle.MultiModelTransactions;
 import org.oobium.guides.views.guides.oobium_models.model_object_lifecycle.Multithreading;
 import org.oobium.guides.views.guides.oobium_models.model_object_lifecycle.TheRequestResponseCycle;
 import org.oobium.guides.views.guides.oobium_models.model_object_lifecycle.Workers;
 import org.oobium.guides.views.guides.oobium_models.model_structure.CrudAndPersistors;
 import org.oobium.guides.views.guides.oobium_models.model_structure.JustAMap;
 import org.oobium.guides.views.guides.oobium_models.model_structure.ModelDescription;
 import org.oobium.guides.views.guides.oobium_models.model_structure.ModelStructure;
 import org.oobium.guides.views.guides.oobium_models.model_structure.TheHierarchy;
 import org.oobium.guides.views.guides.oobium_models.observers.CreatingObservers;
 import org.oobium.guides.views.guides.oobium_models.observers.HaltingExecution;
 import org.oobium.guides.views.guides.oobium_models.observers.ObserverMethods;
 import org.oobium.guides.views.guides.oobium_models.observers.Observers;
 import org.oobium.guides.views.guides.oobium_models.observers.RegisteringObservers;
import org.oobium.guides.views.guides.oobium_models.validation.AddingValidations;
import org.oobium.guides.views.guides.oobium_models.validation.ConditionalValidation;
import org.oobium.guides.views.guides.oobium_models.validation.DisplayingValidationErrors;
import org.oobium.guides.views.guides.oobium_models.validation.UsingValidationErrors;
 import org.oobium.guides.views.guides.oobium_models.validation.Validation;
import org.oobium.guides.views.guides.oobium_models.validation.WhenDoesValidationHappen;
import org.oobium.guides.views.guides.oobium_models.validation.WhyUseValidations;
 
 public class OobiumModels extends Guide {
 
 	public OobiumModels() {
 		super(new Intro());
 		summary = "This guide covers the Models used by Oobium.";
 		chapters = new Chapter[] {
 			/*1*/ new Chapter(new ModelStructure(), new View[] {
 									new JustAMap(),
 									new TheHierarchy(),
 									new ModelDescription(),
 									new CrudAndPersistors()
 								}),
 								
 			/*2*/ new Chapter(new ModelObjectLifecycle(), new View[] {
 									new CachingAndSessions(),
 									new Multithreading(),
 									new TheRequestResponseCycle(),
 									new Workers(),
 									new CustomCode(),
 									new MultiModelTransactions()
 								}),
 
			/*3*/ new Chapter(new Validation(), new View[] {
									new WhyUseValidations(),
									new AddingValidations(),
									new WhenDoesValidationHappen(),
									new ConditionalValidation(),
									new UsingValidationErrors(),
									new DisplayingValidationErrors()
								}),
 
 			/*4*/ new Chapter(new Observers(), new View[] {
 									new CreatingObservers(),
 									new RegisteringObservers(),
 									new ObserverMethods(),
 									new HaltingExecution()
 								}),
 		};
 	}
 
 }

 package ua.kharkov.kture.ot.view.swing.additional.optimization;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.inject.Provider;
 import com.google.inject.Provides;
 import com.google.inject.name.Named;
 import com.google.inject.name.Names;
 import ua.kharkov.kture.ot.common.eventbus.EventBus;
 import ua.kharkov.kture.ot.common.localization.MessageBundle;
 import ua.kharkov.kture.ot.common.localization.StringBasedBundle;
 import ua.kharkov.kture.ot.common.navigation.Navigator;
 import ua.kharkov.kture.ot.shared.OptimizerCriterionKeeper;
 import ua.kharkov.kture.ot.shared.navigation.places.OptimizationPlace;
 import ua.kharkov.kture.ot.view.declaration.additionalwindows.optimization.Criteria;
 import ua.kharkov.kture.ot.view.declaration.additionalwindows.optimization.MinimizationOptimizationCriteria;
 import ua.kharkov.kture.ot.view.swing.additional.AbstractWindowModule;
 import ua.kharkov.kture.ot.view.swing.additional.errors.ErrorMessageWindow;
 import ua.kharkov.kture.ot.view.swing.innerrepo.ViewInnerRepository;
 
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * @author: Stanislav Kurilin
  * @mail: st dot kurilin at gmail dot com
  * Date: 25.04.11
  */
 public class OptimizationModule extends AbstractWindowModule {
     Provider<ViewInnerRepository> repo;
 
     public OptimizationModule(EventBus eventBus) {
         super(eventBus);
     }
 
     @Override
     protected void configure() {
         repo = getProvider(ViewInnerRepository.class);
         final Provider<OptimizationWindow> windowProvider = getProvider(OptimizationWindow.class);
         final Provider<ErrorMessageWindow> errorsProvider = getProvider(ErrorMessageWindow.class);
         final Provider<Navigator> navigator = getProvider(Navigator.class);
         bindPlace(OptimizationPlace.class)
                 .onValidationFailErrorMessage("variantsNotFilled")
                 .toWindow(OptimizationWindow.class);
         MessageBundle bundle = new StringBasedBundle("Optimization", new Locale("ru", "RU"));
         bind(MessageBundle.class).annotatedWith(Names.named("optimization")).toInstance(bundle);
     }
 
 
     @Provides
     @Named("coordinate")
     Map<String, MinimizationOptimizationCriteria> providesCoordinates(@Named("optimization") MessageBundle bundle, OptimizerCriterionKeeper defaultCriterion) {
         return ImmutableMap.<String, MinimizationOptimizationCriteria>builder()
                 .put(bundle.getMessage("cost"), Criteria.COST)
                 .put(bundle.getMessage("crashProbability"), Criteria.CRASH_PROBABILITY)
                 .put(bundle.getMessage("defaultCriterion"), defaultCriterion)
                 .build();
     }
 
     @Provides
     @Named("criteria")
     Map<String, MinimizationOptimizationCriteria> providesCriteria(@Named("optimization") MessageBundle bundle, OptimizerCriterionKeeper defaultCriterion) {
         return ImmutableMap.<String, MinimizationOptimizationCriteria>builder()
                .put(bundle.getMessage("cost"), Criteria.COST)
                .put(bundle.getMessage("crashProbability"), Criteria.CRASH_PROBABILITY)
                 .put(bundle.getMessage("costDivProb"), Criteria.COST_DIVIDED_BY_PROBABILITY_KOEF)
                 .put(bundle.getMessage("costMultProb"), Criteria.COST_MULTI_PROBABILITY)
                 .build();
     }
 }

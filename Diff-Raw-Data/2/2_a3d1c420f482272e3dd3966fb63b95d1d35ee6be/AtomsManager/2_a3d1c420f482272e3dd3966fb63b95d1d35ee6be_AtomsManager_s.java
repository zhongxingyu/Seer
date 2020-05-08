 package infra.ca;
 
 import infra.ca.impl.AtomPOJOPush;
 import infra.ca.strategy.AtomStrategy;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.stereotype.Service;
 import infra.ca.ex.CreativeAtomException;
 import infra.ca.ex.NoTypeStrategyFound;
 import infra.ca.impl.AtomPOJO;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.file.Files;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * @author alari
  * @since 11/13/12 11:41 PM
  */
 @Service
 public class AtomsManager implements ApplicationContextAware {
     private Class<? extends Atom> atomClass = AtomPOJO.class;
     private Class<? extends AtomPush> atomPushClass = AtomPOJOPush.class;
 
     private Map<String, AtomStrategy> strategies = new TreeMap<String, AtomStrategy>();
     private LinkedList<AtomStrategy> strategyDiscoverySequence = new LinkedList<AtomStrategy>();
 
     /**
      * Caches strategy beans for further use
      *
      * @param applicationContext app main context
      * @throws org.springframework.beans.BeansException
      *
      */
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         for (Map.Entry<String, AtomStrategy> e : applicationContext.getBeansOfType(AtomStrategy.class).entrySet()) {
             AtomStrategy strategy = e.getValue();
             assert strategy != null;
             assert strategy.getName() != null && !strategy.getName().isEmpty();
             assert strategies != null;
             strategies.put(strategy.getName(), strategy);
 
             if (strategy.checkLast()) strategyDiscoverySequence.addLast(strategy);
             else strategyDiscoverySequence.addFirst(strategy);
         }
     }
 
     /**
      * Builds an Atom from user's JSON push
      *
      * @param data user provided possibly untyped build data
      * @return Atom built
      * @throws IllegalAccessException
      * @throws InstantiationException
      * @throws CreativeAtomException
      */
     public Atom build(AtomPush data) throws IllegalAccessException, InstantiationException, CreativeAtomException {
         if (data == null) {
             throw new NullPointerException();
         }
         AtomStrategy strategy = null;
         if (data.getType() != null) strategy = strategies.get(data.getType());
         Atom atom = atomClass.newInstance();
 
         prepareExtendedInfo(data);
 
         if (strategy == null) {
             for (AtomStrategy s : strategyDiscoverySequence) {
                 try {
                     if (s.isContentSupported(data)) {
                         strategy = s;
                         atom.setType(s.getName());
                         break;
                     }
                 } catch (Exception e) {
                     continue;
                 }
             }
             if (strategy == null) {
                 throw new NoTypeStrategyFound();
             }
         }
         atom.setDateCreated(new Date());
 
         strategy.update(atom, data);
         return atom;
     }
 
     /**
      * Prepares atom extended info used by strategies to understand the content
      *
      * @param data user given data
      */
     private void prepareExtendedInfo(AtomPush data) {
         // Preparing external url info
         if (data.getExternalUrl() != null && !data.getExternalUrl().isEmpty()) {
             String externalUrl = data.getExternalUrl();
             if (externalUrl.startsWith("//")) {
                 externalUrl = "http:".concat(externalUrl);
             } else if (!externalUrl.startsWith("http://") && !externalUrl.startsWith("https://")) {
                 externalUrl = "http://".concat(externalUrl);
             }
             try {
                 data.setUrl(new URL(externalUrl));
             } catch (MalformedURLException e) {
                 data.setExternalUrl(null);
             }
         }
         // Preparing uploaded file info
        if (data.getFile() != null) {
             try {
                 data.setFileType(Files.probeContentType(data.getFile().toPath()));
             } catch (IOException e) {
                 data.setFile(null);
             }
         }
     }
 
     /**
      * Returns strategy by atom according to atom's type
      *
      * @param atom to retrieve strategy for
      * @return strategy
      * @throws NoTypeStrategyFound if strategy for atom's type is not found
      */
     private AtomStrategy getStrategy(final Atom atom) throws NoTypeStrategyFound {
         AtomStrategy strategy = strategies.get(atom.getType());
         if (strategy == null) {
             throw new NoTypeStrategyFound();
         }
         return strategy;
     }
 
     /**
      * Updates atom with user provided push data
      *
      * @param atom to be updated
      * @param data to update with
      * @throws CreativeAtomException
      */
     public void update(Atom atom, AtomPush data) throws CreativeAtomException {
         prepareExtendedInfo(data);
         getStrategy(atom).update(atom, data);
     }
 
     /**
      * Prepares atom content to render update form
      *
      * @param atom to be updated
      * @throws CreativeAtomException
      */
     public void forUpdate(Atom atom) throws CreativeAtomException {
         getStrategy(atom).forUpdate(atom);
     }
 
     /**
      * Prepares atom content to render to user
      *
      * @param atom to be rendered
      * @throws CreativeAtomException
      */
     public void forRender(Atom atom) throws CreativeAtomException {
         getStrategy(atom).forRender(atom);
     }
 
     /**
      * Processes atom content deleting
      *
      * @param atom to be deleted
      * @throws CreativeAtomException
      */
     public void delete(Atom atom) throws CreativeAtomException {
         getStrategy(atom).delete(atom);
     }
 }

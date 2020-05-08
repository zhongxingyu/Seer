 package org.atlasapi.persistence.content.people;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import org.atlasapi.media.entity.Person;
 import org.atlasapi.media.entity.Publisher;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 
 import com.google.common.base.Optional;
 import com.metabroadcast.common.ids.IdGenerator;
 
 
 public class IdSettingPersonStoreTest {
 
     private final PersonStore delegate = mock(PersonStore.class);
     private final IdGenerator idGenerator = mock(IdGenerator.class);
     private final PersonStore store = new IdSettingPersonStore(delegate, idGenerator);
     
     @Test
     public void testSetsIdOnNewPerson() {
         
         Person person = new Person("uri", "curie", Publisher.BBC);
         
         when(delegate.person(person.getCanonicalUri()))
            .thenReturn(null);
         when(idGenerator.generateRaw()).thenReturn(1L);
         
         store.createOrUpdatePerson(person);
         
         ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
         verify(delegate).createOrUpdatePerson(personCaptor.capture());
         
         assertThat(personCaptor.getValue().getId(), is(1L));
     }
 
     @Test
     public void testSetsIdOnExistingPersonWithoutId() {
         
         Person person = new Person("uri", "curie", Publisher.BBC);
         
         when(delegate.person(person.getCanonicalUri()))
             .thenReturn(Optional.of(new Person("uri", "curie", Publisher.BBC)));
         when(idGenerator.generateRaw())
             .thenReturn(1L);
         
         store.createOrUpdatePerson(person);
         
         ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
         verify(delegate).createOrUpdatePerson(personCaptor.capture());
         
         assertThat(personCaptor.getValue().getId(), is(1L));
     }
 
     @Test
     public void testUsesIdOnExistingPerson() {
         
         Person person = new Person("uri", "curie", Publisher.BBC);
         
         Person existing = new Person("uri", "curie", Publisher.BBC);
         existing.setId(5L);
         
         when(delegate.person(person.getCanonicalUri()))
             .thenReturn(Optional.of(existing));
         
         store.createOrUpdatePerson(person);
         
         verify(idGenerator, never()).generateRaw();
         
         ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
         verify(delegate).createOrUpdatePerson(personCaptor.capture());
         
         assertThat(personCaptor.getValue().getId(), is(5L));
     }
 
 }

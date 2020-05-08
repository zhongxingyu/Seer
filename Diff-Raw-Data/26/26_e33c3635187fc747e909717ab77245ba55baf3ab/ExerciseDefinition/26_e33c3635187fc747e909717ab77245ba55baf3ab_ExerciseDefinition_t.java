 package ch.guitarpracticebuddy.domain;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import javafx.beans.binding.IntegerBinding;
 import javafx.beans.property.*;
 import lombok.Getter;
 import lombok.Setter;
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 import org.joda.time.LocalDate;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import static ch.lambdaj.Lambda.*;
 import static org.hamcrest.Matchers.equalTo;
 
 @Getter
 @Entity
 public class ExerciseDefinition {
 
     @Id
     @GeneratedValue
     private int id;
     @Setter
     private String code;
     private String title;
     private String description;
     private int minutes = 5;
     private int bpm = 120;
     private Rating rating = Rating.BEGINNER;
     private List<Tag> tags = new ArrayList<>();
     @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
     private List<ExerciseAttachment> attachments = new ArrayList<>();
     @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
     private List<ExerciseInstance> plannedInstances = new ArrayList<>();
     private List<ExerciseInstance> pastInstances = new ArrayList<>();
     @Transient
     private IntegerProperty minutesProperty;
     @Transient
     private StringProperty titleProperty;
     @Transient
     private StringProperty descriptionProperty;
     @Transient
     private IntegerProperty bpmProperty;
     @Transient
     private ObjectProperty<Rating> ratingProperty;
     @Transient
     private ListProperty<Tag> tagsProperty;
     @Transient
     private ListProperty<ExerciseInstance> plannedInstancesProperty;
     @Transient
     private StringProperty attachmentsProperty;
 
     public static String createAttachmentString(List<String> paths) {
         return Joiner.on("\n")
                 .skipNulls()
                 .join(paths);
     }
 
     public ExerciseInstance getTodaysExercises() {
         return selectFirst(plannedInstances,
                 having(on(ExerciseInstance.class).isForToday(), equalTo(true)));
     }
 
     public ExerciseInstance getExerciseForDay(LocalDate date) {
         for (ExerciseInstance exerciseInstance : getPlannedInstances()) {
             if (exerciseInstance.getDay().toLocalDate().equals(date)) {
                 return exerciseInstance;
             }
         }
         return null;
     }
 
     public void createInstancesForEntireWeek(Interval interval) {
         for (LocalDate localDate : new LocalDateRange(interval)) {
            if (getExerciseForDay(localDate) == null) {
                ExerciseInstance instance = new ExerciseInstance(localDate.toDateTimeAtCurrentTime());
                instance.setExerciseDefinition(this);
                instance.setBpm(calculateInitialBpm());
                plannedInstances.add(instance);
            }

         }
     }
 
     public List<ExerciseInstance> getPlannedInstances() {
         return plannedInstances;
     }
 
     public boolean isPlannedForToday() {
         return getTodaysExercises() != null;
     }
 
     public int getSeconds() {
         return getMinutes() * 60;
     }
 
     public int getMiliSeconds() {
         return getSeconds() * 1000;
     }
 
     public void removeTag(Tag tag) {
         tags.remove(tag);
     }
 
     public void deleteInstances(Iterable<ExerciseInstance> exerciseInstances) {
         for (ExerciseInstance exerciseInstance : exerciseInstances) {
             if (!exerciseInstance.isDone()) {
                 plannedInstances.remove(exerciseInstance);
             }
         }
     }
 
     public int getBpm() {
 
         return bpm;
     }
 
     private int calculateInitialBpm() {
         List<ExerciseInstance> instancesNotInFuture = select(plannedInstances, having(on(ExerciseInstance.class).getDay(), new IsNotInFuture()));
         ExerciseInstance exerciseInstanceWithMaxBpm = selectMax(instancesNotInFuture, on(ExerciseInstance.class).getDay());
         if (exerciseInstanceWithMaxBpm != null && exerciseInstanceWithMaxBpm.getBpm() > 0) {
             return exerciseInstanceWithMaxBpm.getBpm();
         }
         return bpm;
     }
 
     public void setTags(List<Tag> tags) {
         this.tags = new ArrayList<>(tags);
     }
 
     public Rating getRating() {
         if (rating == null) {
             return Rating.BEGINNER;
         }
         return rating;
     }
 
     public String getAttachmentsAsString() {
         return createAttachmentString(collect(getAttachments(), on(ExerciseAttachment.class).getFilePath()));
     }
 
     public void setAttachmentsAsString(String content) {
         internalSetAttachment(content);
         attachmentsAsStringProperty().set(content);
     }
 
     private void internalSetAttachment(String content) {
         List<ExerciseAttachment> attachments = new ArrayList<>();
 
         for (String filePath : Splitter.on("\n").omitEmptyStrings().split(content)) {
             attachments.add(new ExerciseAttachment(filePath));
         }
         this.attachments = attachments;
     }
 
     public void setTitle(String title) {
         this.title = title;
         titleProperty().set(title);
     }
 
     public void setDescription(String description) {
         this.description = description;
         descriptionProperty().set(title);
 
     }
 
     public void setMinutes(int minutes) {
         this.minutes = minutes;
         minutesProperty().set(minutes);
     }
 
     public void setRating(Rating rating) {
         this.rating = rating;
         ratingProperty().set(rating);
     }
 
     public void setBpm(int bpm) {
         this.bpm = bpm;
         bpmProperty().set(bpm);
     }
 
    public String getFolder() {
        if (code != null) {
            return code;
        }
        return getTitle().toLowerCase().replaceAll(" ", "_").replaceAll("\\W+", "");
    }

     private static class IsNotInFuture extends BaseMatcher<DateTime> {
         @Override
         public boolean matches(Object o) {
             DateTime dateTime = (DateTime) o;
             return dateTime.isBefore(DateTime.now().withTimeAtStartOfDay().plusDays(1));
         }
 
         @Override
         public void describeTo(Description description) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
     }
 
     // ====================================== PROPERTIES ======================================
 
     public IntegerProperty minutesProperty() {
         if (this.minutesProperty == null) {
             this.minutesProperty = new SimpleIntegerProperty(this, "minutes") {
                 @Override
                 public void set(int i) {
                     super.set(i);
                     minutes = i;
                 }
             };
             this.minutesProperty.set(getMinutes());
         }
         return minutesProperty;
     }
 
     public StringProperty titleProperty() {
         if (this.titleProperty == null) {
             this.titleProperty = new SimpleStringProperty(this, "title") {
                 @Override
                 public void set(String s) {
                     super.set(s);
                     title = s;
                 }
             };
             this.titleProperty.set(getTitle());
         }
         return titleProperty;
     }
 
     public StringProperty descriptionProperty() {
         if (this.descriptionProperty == null) {
             this.descriptionProperty = new SimpleStringProperty(this, "description") {
                 @Override
                 public void set(String s) {
                     super.set(s);
                     description = s;
                 }
             };
             this.descriptionProperty.set(getDescription());
         }
         return descriptionProperty;
     }
 
     public IntegerProperty bpmProperty() {
         if (this.bpmProperty == null) {
             this.bpmProperty = new SimpleIntegerProperty(this, "bpm") {
                 @Override
                 public void set(int value) {
                     super.set(value);
                     bpm = value;
                 }
             };
             this.bpmProperty.set(getBpm());
         }
         return bpmProperty;
     }
 
     public ObjectProperty<Rating> ratingProperty() {
         if (this.ratingProperty == null) {
             this.ratingProperty = new SimpleObjectProperty<Rating>(this, "rating") {
                 @Override
                 public void set(Rating value) {
                     super.set(value);
                     rating = value;
                 }
             };
             this.ratingProperty.set(getRating());
         }
         return ratingProperty;
     }
 
     public StringProperty attachmentsAsStringProperty() {
         if (this.attachmentsProperty == null) {
             this.attachmentsProperty = new SimpleStringProperty(this, "attachments") {
                 @Override
                 public void set(String value) {
                     super.set(value);
                     internalSetAttachment(value);
                 }
             };
             this.attachmentsProperty.set(getAttachmentsAsString());
         }
         return attachmentsProperty;
     }
 
     public IntegerBinding milisecondsProperty() {
 
         return minutesProperty().multiply(60).multiply(1000);
     }
 }

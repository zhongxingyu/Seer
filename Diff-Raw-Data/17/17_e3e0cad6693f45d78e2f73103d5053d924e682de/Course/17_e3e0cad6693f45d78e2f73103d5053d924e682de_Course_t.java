 package no.hials.muldvarpweb.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlTransient;
 import no.hials.muldvarpweb.fragments.Fragment;
 
 /**
  *
  * @author kristoffer
  */
 @Entity
 @Table(name = "course")
 public class Course implements Serializable {
     @Id
     @GeneratedValue
     private Integer id;
     
     @Column(name = "name")
     String name;
     
     @Column(name = "detail")
     String detail;
     
     @Column(name = "description")
     String description;
     
     @Column(name = "imageurl")
     String imageurl;
     
     @Column(name = "revision")
     Integer revision;
     
     @Temporal(javax.persistence.TemporalType.DATE)
     @Column(name = "revision_date")
     Date revision_date;
     
     @OneToMany
     List<Theme> themes;
     
     @OneToMany
     List<ObligatoryTask> obligatoryTasks;
     
     @OneToMany
     List<Exam> exams;
     
     @ManyToMany
     List<Person> teachers;
     
     @ManyToMany
     List<Video> videos;
     
     @ManyToMany
     List<LibraryItem> documents;
     
     @ManyToMany
     List<Article> news;
     
    @ManyToMany(mappedBy = "courses")
     List<Programme> programmes;
     
     @OneToMany
     List<Quiz> quizzes;
     
     // temp greier til vi lager noe bedre
     @ManyToOne
     Article info;
     
     @ManyToOne
     Article dates;
     
     @ManyToOne
     Article help;
    
     public Course() {
         this.revision = 0;
         this.revision_date = new Date();
     }
 
     public Course(String name, String detail, String imageurl, Integer revision, List<Theme> themes, List<ObligatoryTask> obligatoryTasks, List<Exam> exams, List<Person> teachers, List<Programme> programme) {
         this.name = name;
         this.detail = detail;
         this.imageurl = imageurl;
         this.revision = revision;
         this.revision_date = new Date();
         this.themes = themes;
         this.obligatoryTasks = obligatoryTasks;
         this.exams = exams;
         this.teachers = teachers;
         this.programmes = programme;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public Course(String name) {
         this.name = name;
     }
 
     public String getDetail() {
         return detail;
     }
 
     public void setDetail(String detail) {
         this.detail = detail;
     }
 
     public String getImageurl() {
         return imageurl;
     }
 
     public void setImageurl(String imageurl) {
         this.imageurl = imageurl;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public List<Person> getTeachers() {
         return teachers;
     }
 
     public void setTeachers(List<Person> teachers) {
         this.teachers = teachers;
     }
     
     public void addTeacher(Person teacher) {
         teachers.add(teacher);
     }
     
     public void removeTeacher(Person teacher) {
         teachers.remove(teacher);
     }
     
     public Integer getRevision() {
         return revision;
     }
 
     public void setRevision(Integer revision) {
         this.revision = revision;
     }
 
     public List<Theme> getThemes() {
         return themes;
     }
 
     public void setThemes(List<Theme> themes) {
         this.themes = themes;
     }
 
     public List<ObligatoryTask> getObligatoryTasks() {
         return obligatoryTasks;
     }
 
     public void setObligatoryTasks(List<ObligatoryTask> obligatoryTasks) {
         this.obligatoryTasks = obligatoryTasks;
     }
 
     public Date getRevision_date() {
         return revision_date;
     }
 
     public void setRevision_date(Date revision_date) {
         this.revision_date = revision_date;
     }
 
     public List<Exam> getExams() {
         return exams;
     }
 
     public void setExams(List<Exam> exams) {
         this.exams = exams;
     }
     
     public void addTheme(Theme theme) {
         themes.add(theme);
     }
     
     public void editTheme(Theme theme) {
         for(int i = 0; i < themes.size(); i++) {
             if(themes.get(i).getId() == theme.getId()) {
                 themes.set(i, theme);
             }
         }
     }
     
     public void removeTheme(Theme theme) {
         themes.remove(theme);
     }
     
     public void addTask(Theme theme, Task task) {
         for(int i = 0; i < themes.size(); i++) {
             if(themes.get(i).getId() == theme.getId()) {
                 themes.get(i).addTask(task);
             }
         }
     }
     
     public void editTask(Theme theme, Task task) {
         for(int i = 0; i < themes.size(); i++) {
             if(themes.get(i).getId() == theme.getId()) {
                 themes.get(i).editTask(task);
             }
         }
     }
     
     public void removeTask(Theme theme, Task task) {
         for(int i = 0; i < themes.size(); i++) {
             if(themes.get(i).getId() == theme.getId()) {
                 themes.get(i).removeTask(task);
             }
         }
     }
     
     public void addObligatoryTask(ObligatoryTask obligtask) {   
         obligatoryTasks.add(obligtask);
     }
     
     public void editObligatoryTask(ObligatoryTask obligtask) {
         for(int i = 0; i < obligatoryTasks.size(); i++) {
             if(obligatoryTasks.get(i).getId() == obligtask.getId()) {
                 obligatoryTasks.set(i, obligtask);
             }
         }
     }
     
     public void removeObligatoryTask(ObligatoryTask obligtask) {
         obligatoryTasks.remove(obligtask);
     }
     
     public void addExam(Exam exam) {
         exams.add(exam);
     }
     
     public void editExam(Exam exam) {
         for(int i = 0; i < exams.size(); i++) {
             if(exams.get(i).getId() == exam.getId()) {
                 exams.set(i, exam);
             }
         }
     }
     
     public void removeExam(Exam exam) {
         exams.remove(exam);
     }
 
 //    public String getDescription() {
 //        return description;
 //    }
 //
 //    public void setDescription(String description) {
 //        this.description = description;
 //    }
 
     @XmlTransient
     public List<Programme> getProgrammes() {
         if(programmes == null) {
             programmes = new ArrayList<Programme>();
         }
         return programmes;
     }
 
     public void setProgrammes(List<Programme> programmes) {
         this.programmes = programmes;
     }
     
     public void addProgramme(Programme p) {
         if(programmes == null) {
             programmes = new ArrayList<Programme>();
         }
         programmes.add(p);
     }
 
     @XmlTransient
     public List<Video> getVideos() {
         return videos;
     }
 
     public void setVideos(List<Video> video) {
         this.videos = video;
     }
     
     public void addVideo(Video video) {
         if(videos == null) {
             videos = new ArrayList<Video>();
         }
         videos.add(video);
     }
 
     public void removeVideo(Video video){
         
         getVideos().remove(video);
     }
 
     public List<LibraryItem> getDocuments() {
         return documents;
     }
     
     public void addDocument(LibraryItem document) {
         if(document == null) {
             documents = new ArrayList<LibraryItem>();
         }
         documents.add(document);
     }
 
     public void removeDocument(LibraryItem document){
         getDocuments().remove(document);
     }
 
     public void setDocuments(List<LibraryItem> documents) {
         this.documents = documents;
     }
 
     public List<Article> getNews() {
         return news;
     }
 
     public void setNews(List<Article> news) {
         this.news = news;
     }
 
     public List<Quiz> getQuizzes() {
         return quizzes;
     }
 
     public void setQuizzes(List<Quiz> quizzes) {
         this.quizzes = quizzes;
     }
 
     public Article getInfo() {
         return info;
     }
 
     public void setInfo(Article info) {
         this.info = info;
     }
 
     public Article getHelp() {
         return help;
     }
 
     public void setHelp(Article help) {
         this.help = help;
     }
 
     public void removeProgramme(Programme p) {
         programmes.remove(p);
     }
 
     public Article getDates() {
         return dates;
     }
 
     public void setDates(Article dates) {
         this.dates = dates;
     }
     
     // eksperimentelle greier
     List<Fragment> fragmentBundle;
 
     public List<Fragment> getFragmentBundle() {
         return fragmentBundle;
     }
 
     public void setFragmentBundle(List<Fragment> fragmentBundle) {
         this.fragmentBundle = fragmentBundle;
     }
     
     public void addFragment(Fragment f) {
         if(fragmentBundle == null) {
             fragmentBundle = new ArrayList<Fragment>();
         }
         fragmentBundle.add(f);
     }
     
     public void removeFragment(Fragment f) {
         fragmentBundle.remove(f);
     }
 }

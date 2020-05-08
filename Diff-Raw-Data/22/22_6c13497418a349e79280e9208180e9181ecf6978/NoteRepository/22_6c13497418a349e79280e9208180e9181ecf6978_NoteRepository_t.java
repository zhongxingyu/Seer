 package com.my.suicidenote.repo;
 
 import java.util.List;
 
 import org.springframework.data.mongodb.repository.MongoRepository;
 import com.my.suicidenote.dto.Note;
 
 public interface NoteRepository extends MongoRepository<Note, Long> {
	List<Note> findByWhenLessThan(long when);	
 }

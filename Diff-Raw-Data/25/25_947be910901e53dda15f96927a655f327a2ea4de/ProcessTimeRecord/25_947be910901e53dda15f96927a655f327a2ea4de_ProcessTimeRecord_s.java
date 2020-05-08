 package org.kalibro.core.persistence.record;
 
 import javax.persistence.*;
 
 import org.kalibro.ProcessState;
 
 /**
  * Java Persistence API entity for mapping times to {@link ProcessState}.
  * 
  * @author Carlos Morais
  */
 @Entity(name = "ProcessTime")
 @Table(name = "\"PROCESS_TIME\"")
 public class ProcessTimeRecord {
 
 	@ManyToOne(optional = false)
 	@SuppressWarnings("unused" /* used by JPA */)
 	@JoinColumn(name = "\"processing\"", nullable = false, referencedColumnName = "\"id\"")
 	private ProcessingRecord processing;
 
 	@Id
 	@GeneratedValue
 	@Column(name = "\"id\"", nullable = false)
 	@SuppressWarnings("unused" /* used by JPA */)
 	private Long id;
 
 	@Column(name = "\"state\"", nullable = false)
 	private ProcessState state;
 
 	@Column(name = "\"time\"", nullable = false)
 	private Long time;
 
 	public ProcessTimeRecord() {
 		super();
 	}
 
	public ProcessTimeRecord(ProcessState state, Long time) {
 		this.state = state;
 		this.time = time;
 	}
 
 	public ProcessState state() {
 		return state;
 	}
 
 	public Long time() {
 		return time;
 	}
 }

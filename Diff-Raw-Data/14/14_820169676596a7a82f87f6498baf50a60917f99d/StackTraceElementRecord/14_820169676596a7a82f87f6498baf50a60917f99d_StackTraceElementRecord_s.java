 package org.kalibro.core.persistence.record;
 
 import javax.persistence.*;
 
 import org.kalibro.dto.StackTraceElementDto;
 
 /**
  * Java Persistence API entity for {@link StackTraceElement}.
  * 
  * @author Carlos Morais
  */
 @Entity(name = "StackTraceElement")
 @Table(name = "\"STACK_TRACE_ELEMENT\"")
 public class StackTraceElementRecord extends StackTraceElementDto {
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "\"throwable\"", referencedColumnName = "\"id\"")
 	private ThrowableRecord throwable;
 
 	@Id
 	@GeneratedValue
 	@Column(name = "\"id\"", nullable = false)
 	private Long id;
 
 	@Column(name = "\"index\"", nullable = false)
 	private Integer index;
 
 	@Column(name = "\"declaring_class\"", nullable = false)
 	private String declaringClass;
 
 	@Column(name = "\"method_name\"", nullable = false)
 	private String methodName;
 
	@Column(name = "\"file_name\"", nullable = false)
 	private String fileName;
 
	@Column(name = "\"line_number\"", nullable = false)
 	private Integer lineNumber;
 
 	public StackTraceElementRecord() {
 		super();
 	}
 
 	public StackTraceElementRecord(StackTraceElement element) {
 		this(element, null, 0);
 	}
 
 	public StackTraceElementRecord(StackTraceElement element, ThrowableRecord throwable, int index) {
 		this.throwable = throwable;
 		this.index = index;
 		declaringClass = element.getClassName();
 		methodName = element.getMethodName();
 		fileName = element.getFileName();
 		lineNumber = element.getLineNumber();
 	}
 
 	@Override
 	public String declaringClass() {
 		return declaringClass;
 	}
 
 	@Override
 	public String methodName() {
 		return methodName;
 	}
 
 	@Override
 	public String fileName() {
 		return fileName;
 	}
 
 	@Override
 	public int lineNumber() {
 		return lineNumber;
 	}
 
 	void addTo(StackTraceElement[] stackTrace) {
 		stackTrace[index] = convert();
 	}
 }

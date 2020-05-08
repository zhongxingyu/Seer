 package au.org.scoutmaster.views.wizards.importer;
 
import javax.validation.ConstraintViolationException;

 public class ImportItemStatus
 {
	public Integer getRow()
	{
		return row;
	}

	public Exception getException()
	{
		return exception;
	}

	Integer row;
	Exception exception;

	public ImportItemStatus(Integer row, Exception e)
	{
		this.row = row;
		this.exception = new ImportRowExeption(e);
	}

	public ImportItemStatus(int row, ConstraintViolationException e)
	{
		this.row = row;
		this.exception = new ImportRowExeption(e);

	}
 
 }

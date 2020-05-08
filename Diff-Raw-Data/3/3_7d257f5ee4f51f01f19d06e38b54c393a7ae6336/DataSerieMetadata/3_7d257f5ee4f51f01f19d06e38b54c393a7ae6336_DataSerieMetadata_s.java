 /**
  *
  */
 package org.ocha.hdx.persistence.entity.metadata;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.ForeignKey;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.curateddata.Source;
 import org.ocha.hdx.persistence.entity.i18n.Text;
 
 /**
  * @author alexandru-m-g
  * 
  */
 @Entity
 @Table(name = "hdx_dataserie_metadata")
 @SequenceGenerator(name = "hdx_dataserie_metadata_seq", sequenceName = "hdx_dataserie_metadata_seq")
 public class DataSerieMetadata {
 
 	public enum EntryKey {
		METHODOLOGY("Methodology"), MORE_INFO("More info"), DATASET_SUMMARY("Dataset summary"), TERMS_OF_USE("Terms of use"), EXPECTED_TIME_FORMAT("Expected time format"), INTERPRETED_START_TIME(
				"Interpreted start time"), INTERPRETED_END_TIME("Interpreted end time"), INTERPRETED_PERIODICITY("Interpreted periodicity"), VALIDATION_NOTES("Validation notes");
 		private final String label;
 
 		private EntryKey(final String label) {
 			this.label = label;
 		}
 
 		public String getLabel() {
 			return label;
 		}
 	}
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO, generator = "hdx_dataserie_metadata_seq")
 	@Column(name = "id", nullable = false)
 	private long id;
 
 	@ManyToOne
 	@ForeignKey(name = "fk__dataserie_metadata_to_source")
 	@JoinColumn(name = "source_id", nullable = false)
 	private Source source;
 
 	@ManyToOne
 	@ForeignKey(name = "fk__dataserie_metadata_to_indicator_type")
 	@JoinColumn(name = "indicator_type_id", nullable = false)
 	private IndicatorType indicatorType;
 
 	@Column(name = "entry_key", nullable = false, updatable = false)
 	@Enumerated(EnumType.STRING)
 	private EntryKey entryKey;
 
 	@ManyToOne
 	@JoinColumn(name = "entry_value_text_id")
 	@ForeignKey(name = "fk__dataserie_metadata_to_name_text")
 	private Text entryValue;
 
 	public DataSerieMetadata() {
 	}
 
 	public DataSerieMetadata(final IndicatorType type, final Source source, final EntryKey entryKey, final Text entryValue) {
 		super();
 		this.source = source;
 		this.indicatorType = type;
 		this.entryKey = entryKey;
 		this.entryValue = entryValue;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(final long id) {
 		this.id = id;
 	}
 
 	public Source getSource() {
 		return source;
 	}
 
 	public void setSource(final Source source) {
 		this.source = source;
 	}
 
 	public IndicatorType getIndicatorType() {
 		return indicatorType;
 	}
 
 	public void setIndicatorType(final IndicatorType indicatorType) {
 		this.indicatorType = indicatorType;
 	}
 
 	public Text getEntryValue() {
 		return entryValue;
 	}
 
 	public void setEntryValue(final Text entryValue) {
 		this.entryValue = entryValue;
 	}
 
 	public EntryKey getEntryKey() {
 		return entryKey;
 	}
 
 	public void setEntryKey(final EntryKey entryKey) {
 		this.entryKey = entryKey;
 	}
 
 }
